# World Flora Online Portal

import 'globals.pp'

require common

# GeoServer
class { 'geoserver':
	geoserver_database_host => "$databse_host",
	geoserver_database_name => "gis",
	geoserver_database_user => "geoserver",
	geoserver_database_password => "gs",
}
require geoserver

# ActiveMQ
class { 'messagebroker':
	max_memory => "64",
	admin_password => "password",
	emonocot_password => "worldflora"
}
require messagebroker

# Portal
class { "wf-portal":
	version => "1.0.6",
}

wf-portal::wf-portal { 'wf-portal':
	recoveryController_baseUrl => "http://${container_hostname}",
	registrationController_baseUrl => "http://${container_hostname}",
	userServiceImpl_temporaryFolder => "/var/spool/worldflora",
	userServiceImpl_userProfilesFolder => "/var/lib/worldflora/images/profiles",
	amq_broker_uri => "tcp://$portal_host:61616",
	amq_broker_userName => "worldflora",
	amq_broker_password => "worldflora",
	amq_readWriteQueue_name => "worldflora.readWriteQueue",
	amq_readOnlyQueue_name => "worldflora.readOnlyQueue",
	solr_url => "http://$database_host:8983/solr",
	http_proxyHost => "",
	http_proxyPort => "",
	email_fromAddress => $developer_email,
	quartz_cron_expression => "0 * * * * ?",
	ehcache_diskstore_path => "/var/cache/tomcat7/work/world-flora-portal/cache",
	ehcache_config_location => "classpath:/META-INF/ehcache.xml",
	web_map_server_url => "/geoserver/wms",

	hibernate_dialect => "org.hibernatespatial.mysql.MySQLSpatialDialect",
	jdbc_driver_classname => "com.mysql.jdbc.Driver",
	jdbc_driver_url => "jdbc:mysql://$database_host:3306/worldflora",
	jdbc_driver_username => "worldflora",
	jdbc_driver_password => "wf",
	liquibase_username => "worldflora",
	liquibase_password => "wf",
	liquibase_changelog => "changelog.mysql.xml",
	liquibase_default_schema_name => "worldflora",
	spring_security_sid_identity_query => "call identity()",
	spring_security_class_identity_query => "call identity()",
	quartz_jobstore_delegate => "org.quartz.impl.jdbcjobstore.StdJDBCDelegate",

	comment_email_subject => "Comment submitted via. World Flora Vagrant",
	comment_emailedTo => "'worldflora@localhost'",
	download_citation_string => "World Flora Citation String",
	download_creator_email => "creator@e-monocot.org",
	download_creator_name => "Creator Name",
	download_meta_description => "Description String",
	download_homepage_url => "http://${container_hostname}",
	download_identifier => "World Flora",
	download_logo_url => "http://${container_hostname}/css/images/logo.png",
	download_publisher_email => "publisher@demo.worldfloraonline.org",
	download_publisher_name => "Publisher Name",
	download_rights => "Rights Statement",
	download_subject => "Subject Keywords",
	download_title => "World Flora Download",
	email_debug => "true",
	email_imap_username => "worldflora",
	email_imap_password => "worldflora",
	email_imap_server => "localhost",
	email_imap_port => "143",
	email_imap_folder => "INBOX",
	email_imap_enable_starttls => "false",
	email_imap_socketfactory_fallback => "true",
	email_imap_socketfactory_class => "javax.net.ssl.SSLSocketFactory",
	portal_baseUrl => "http://${container_hostname}",
	harvester_baseUrl => "http://$harvester_host/world-flora-harvest",
	harvester_admin_password => "letmein",
	harvester_services_client_identifier => "devvm.ad.kew.org",
	portalWebservice_harvester_password => "portal",
	portalWebservice_harvester_username => "portal",
}
