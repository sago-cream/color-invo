require 'minitest/autorun'
require_relative 'release_config'

class ReleaseConfigTest < Minitest::Test
  def env
    { 'PLAY_SERVICE_ACCOUNT_JSON' => '/tmp/test-service-account.json',
      'ANDROID_AAB_PATH' => '/tmp/test.aab', 'ANDROID_VERSION_CODE' => '17' }
  end

  def test_upload_defaults_to_validation_of_an_internal_draft_with_full_listing
    config = ColorInvoRelease.upload(env)
    assert_equal 'internal', config[:track]
    assert_equal 'draft', config[:release_status]
    assert config[:validate_only]
    refute config[:skip_upload_images]
    refute config[:skip_upload_screenshots]
    assert_equal 'dev.hsichen.colorinvo', config[:package_name]
  end

  def test_explicit_commit_and_completed_status_are_preserved
    config = ColorInvoRelease.upload(env.merge('PLAY_VALIDATE_ONLY' => 'false', 'PLAY_RELEASE_STATUS' => 'completed'))
    refute config[:validate_only]
    assert_equal 'completed', config[:release_status]
  end

  def test_promote_filters_the_exact_tested_version_without_uploading_a_bundle
    config = ColorInvoRelease.promote(env.merge('PLAY_TRACK' => 'production'))
    assert_equal 17, config[:version_code]
    assert_equal 'internal', config[:track]
    assert_equal 'production', config[:track_promote_to]
    assert config[:skip_upload_aab]
    assert config[:skip_upload_metadata]
    assert config[:skip_upload_changelogs]
    refute config.key?(:aab)
  end

  def test_same_track_promotion_is_rejected
    assert_raises(ArgumentError) { ColorInvoRelease.promote(env.merge('PLAY_TRACK' => 'internal')) }
  end

  def test_invalid_values_fail_before_any_api_call
    %w[0 -1 01 2100000001 999999999999999999].each do |value|
      assert_raises(ArgumentError) { ColorInvoRelease.upload(env.merge('ANDROID_VERSION_CODE' => value)) }
    end
    { 'PLAY_TRACK' => 'typo', 'PLAY_RELEASE_STATUS' => 'finished', 'PLAY_VALIDATE_ONLY' => 'yes' }.each do |key, value|
      assert_raises(ArgumentError) { ColorInvoRelease.upload(env.merge(key => value)) }
    end
  end
end
