# Pure configuration shared by the upload/promotion lanes and their offline tests.
module ColorInvoRelease
  TRACKS = %w[internal alpha beta production].freeze
  STATUSES = %w[draft completed].freeze

  def self.choice(env, key, fallback, values)
    value = env.fetch(key, fallback)
    raise ArgumentError, "#{key} must be one of #{values.join(', ')}" unless values.include?(value)
    value
  end

  def self.version_code(env)
    value = env.fetch('ANDROID_VERSION_CODE')
    raise ArgumentError, 'ANDROID_VERSION_CODE must be between 1 and 2100000000' unless value.match?(/\A[1-9][0-9]{0,9}\z/) && value.to_i <= 2_100_000_000
    value.to_i
  end

  def self.common(env)
    {
      package_name: 'dev.hsichen.colorinvo',
      json_key: env.fetch('PLAY_SERVICE_ACCOUNT_JSON'),
      validate_only: choice(env, 'PLAY_VALIDATE_ONLY', 'true', %w[true false]) == 'true'
    }
  end

  def self.upload(env)
    common(env).merge(
      aab: env.fetch('ANDROID_AAB_PATH'),
      version_code: version_code(env),
      track: choice(env, 'PLAY_TRACK', 'internal', TRACKS),
      release_status: choice(env, 'PLAY_RELEASE_STATUS', 'draft', STATUSES),
      metadata_path: File.expand_path('../play', __dir__),
      skip_upload_images: false,
      skip_upload_screenshots: false,
      sync_image_upload: true
    )
  end

  def self.promote(env)
    source = choice(env, 'PLAY_SOURCE_TRACK', 'internal', TRACKS)
    target = choice(env, 'PLAY_TRACK', 'beta', TRACKS)
    raise ArgumentError, 'Promotion source and destination must differ' if source == target
    common(env).merge(
      track: source,
      track_promote_to: target,
      version_code: version_code(env),
      track_promote_release_status: choice(env, 'PLAY_RELEASE_STATUS', 'draft', STATUSES),
      skip_upload_apk: true,
      skip_upload_aab: true,
      skip_upload_metadata: true,
      skip_upload_changelogs: true,
      skip_upload_images: true,
      skip_upload_screenshots: true
    )
  end
end
