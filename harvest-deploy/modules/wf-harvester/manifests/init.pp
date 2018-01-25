class wf-harvester ($version) {
}

define wf-harvester::wf-harvester (
	$version = '1.0.6',

	$recoveryController_baseUrl,
	$registrationController_baseUrl,
	$userServiceImpl_searchPath = "/usr/bin",
	$userServiceImpl_temporaryFolder,
	$userServiceImpl_userProfilesFolder,
	$taxonDaoImpl_rootRank = "ORDER",
	$amq_broker_uri,
	$amq_broker_userName,
	$amq_broker_password,
	$amq_readWriteQueue_name,
	$amq_readOnlyQueue_name,
	$solr_url,
	$logging_level = "DEBUG",
	$logging_method = "file",
	$http_proxyHost,
	$http_proxyPort,
	$email_smtp_username = "",
	$email_smtp_password = "",
	$email_smtp_host = "localhost",
	$email_smtp_port = "25",
	$email_smtp_auth = "false",
	$email_smtp_enable_starttls = "false",
	$email_fromAddress,
	$quartz_cron_expression,
	$ehcache_diskstore_path,
	$ehcache_config_location,
	$web_map_server_url,

	$hibernate_dialect,
	$jdbc_driver_classname,
	$jdbc_driver_url,
	$jdbc_driver_username,
	$jdbc_driver_password,
	$liquibase_username,
	$liquibase_password,
	$liquibase_changelog,
	$liquibase_default_schema_name,
	$liquibase_drop_database_first = "false",
	$spring_security_sid_identity_query,
	$spring_security_class_identity_query,
	$quartz_jobstore_delegate,

	$batch_job_dir = "/var/cache/tomcat7/worldflora",
	$worldflora_base_dir = "/var/lib/worldflora",
	$harvester_cache_dir = "/var/cache/tomcat7/worldflora-harvester",
	$harvester_imagemagick_path = "/usr/bin",
	$worldflora_spool_dir = "/var/spool/worldflora",
	$solr_core = "collection1",
	$solr_max_segments = "5",
	$comment_email_subject,
	$comment_emailedTo,
	$download_citation_string,
	$download_creator_email,
	$download_creator_name,
	$download_meta_description,
	$download_homepage_url,
	$download_identifier,
	$download_logo_url,
	$download_publisher_email,
	$download_publisher_name,
	$download_rights,
	$download_subject,
	$download_title,
	$email_debug,
	$email_imap_username,
	$email_imap_password,
	$email_imap_server,
	$email_imap_port,
	$email_imap_folder,
	$email_imap_enable_starttls,
	$email_imap_socketfactory_fallback,
	$email_imap_socketfactory_class,
	$harvester_baseUrl,
	$harvester_admin_password,
	$harvester_services_client_identifier,
	$portalWebservice_harvester_password,
	$portalWebservice_harvester_username,
	$portal_baseUrl,
	$document_root = "/var/www"
	) {
	include tomcat7

	user { 'worldflora':
		ensure => 'present',
		comment => 'World Flora',
		home => $worldflora_base_dir,
		# mkpasswd -m sha-512
		password => '$6$YPvPJpr9yfLUR$UXWVV2bT8jmtEuTyPsLDDmJFlPMb7BJLaR/goY2P8r6o406vYF6hHEtCVTM4OidRLAsn4wfZaQdS1TYGELfl//',
		system => 'true',
	} ->
	file { "${worldflora_base_dir}":
		ensure => "directory",
		owner  => "worldflora",
		group  => "worldflora",
		mode   => 755,
	}

	# • Imagemagick • #
	package { 'imagemagick':
		ensure => "present",
	} ->
	file { ["/var/lib/tomcat7/common/classes/META-INF", "/var/lib/tomcat7/common/classes/META-INF/spring"]:
		ensure => directory,
	} ->

	# • Harvester configuration • #
	file { "/var/lib/tomcat7/common/classes/META-INF/spring/emonocot-harvest.properties":
		ensure  => file,
		content => template('wf-harvester/emonocot-harvest.properties.erb'),
	} ->

	file { "/var/lib/tomcat7/common/classes/META-INF/spring/emonocot-harvest-db.properties":
		ensure  => file,
			content => template('wf-harvester/emonocot-harvest-db.properties.erb'),
	} ->

	file { "/var/lib/tomcat7/common/classes/logback-emonocot-harvest.xml":
		ensure  => file,
		content => template('wf-harvester/logback-emonocot-harvest.xml.erb'),
	}

	file { [
		"${batch_job_dir}",
		"${harvester_cache_dir}",
		"${worldflora_base_dir}/images",
		"${worldflora_base_dir}/images/fullsize",
		"${worldflora_base_dir}/images/thumbnails",
		"${worldflora_base_dir}/output",
		"${worldflora_spool_dir}",
		"${worldflora_spool_dir}/sitemap",
		]:
		ensure => "directory",
		owner  => "tomcat7",
		group  => "tomcat7",
		mode   => 755,
	}

	file { "/var/lib/tomcat7/webapps/world-flora-harvest.war":
		owner => tomcat7,
		group => tomcat7,
		ensure => file,
		source => "puppet:///modules/wf-harvester/emonocot-harvest-${version}.war",
	}

	# Delete files older than 24 hours from the download output folder
	cron { "cron-harvester-remove-old-files":
		command => "/usr/bin/find ${worldflora_base_dir}/output/ -mindepth 1 -mtime +1 -exec rm {} \\;",
		user => 'root', # ?
		hour => 12,
		minute  => 00,
		ensure => present,
	}

	# IMAP mailbox (temporary)
	file { [
		"$worldflora_base_dir/Maildir",
		"$worldflora_base_dir/Maildir/cur",
		"$worldflora_base_dir/Maildir/new",
		"$worldflora_base_dir/Maildir/tmp",
		"$worldflora_base_dir/Maildir/.INBOX",
		"$worldflora_base_dir/Maildir/.INBOX/cur",
		"$worldflora_base_dir/Maildir/.INBOX/new",
		"$worldflora_base_dir/Maildir/.INBOX/tmp"]:
		ensure => directory,
		owner => worldflora,
		group => worldflora,
		mode => "0700"
	}

	# • Static files • #
	# Set permissions for deployments using Maven & Wagon
	file { [ "${document_root}",
		 "${document_root}/js",
		 "${document_root}/css",
		 "${document_root}/tiles"]:
		ensure => "directory",
		owner  => "mb10kg",
		group  => "tomcat7",
		mode   => 775,
		before => Service['tomcat7'],
	}

	# Symlink images/output directories into Apache web root
	file { "${document_root}/images":
		ensure => "link",
		target => "${worldflora_base_dir}/images",
		before => Service['tomcat7'],
	}

	file { "${document_root}/output":
		ensure => "link",
		target => "${worldflora_base_dir}/output",
		before => Service['tomcat7'],
	}

	file { "${document_root}/sitemap":
		ensure => "link",
		target => "${worldflora_spool_dir}/sitemap",
		before => Service['tomcat7'],
	}

}
