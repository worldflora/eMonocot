/** * Geoserver
 */
class geoserver (
	$geoserver_data_dir = "/var/lib/geoserver",
	$users = { geoserver => { 'username' => 'geoserver', 'password' => 'g3053rv3r' }},
	$geoserver_database_password,
	$geoserver_database_host,
	$geoserver_database_name,
	$geoserver_database_user,
	$document_root = "/var/www",
) {
	# install the geoserver archive
	# this URL no longer resolves so using local copy 2016-09-19 wriley
#	wget::fetch { 'fetch-geoserver':
#		source      => "http://web.ad.kew.org/~mb10kg/vm/geoserver.war",
#		destination => "/var/tmp/geoserver.war",
#	} ->
	file { "/var/lib/tomcat7/webapps/geoserver.war" :
		ensure  => file,
		mode => 644,
		owner => tomcat7,
		group => tomcat7,
#		source => "/var/tmp/geoserver.war",
		source => "puppet:///modules/geoserver/geoserver.war",
#		require => Class["wf-harvester"],
	}

#	file { [ "${document_root}",
#		 "${document_root}/js",
#		 "${document_root}/css",
#		 "${document_root}/tiles"]:
#		ensure => "directory",
#		owner  => "mb10kg",
#		group  => "tomcat7",
#		mode   => 775,
#		before => Service['tomcat7'],
#	}

#	# Symlink images/output directories into Apache web root
#	file { "${document_root}/images":
#		ensure => "link",
#		target => "${worldflora_base_dir}/images",
#		before => Service['tomcat7'],
#	}

#	file { "${document_root}/output":
#		ensure => "link",
#		target => "${worldflora_base_dir}/output",
#		before => Service['tomcat7'],
#	}

#	file { "${document_root}/sitemap":
#		ensure => "link",
#		target => "${worldflora_spool_dir}/sitemap",
#		before => Service['tomcat7'],
#	}

	# create the geoserver data directory
		file {[
			"${geoserver_data_dir}",
			"${geoserver_data_dir}/security",
			"${geoserver_data_dir}/styles"
		]:
		ensure  => directory,
		mode => 755,
		owner => tomcat7,
		group => tomcat7,
	}

	# unpack Geoserver data files
	exec { "unzip /var/lib/tomcat7/webapps/geoserver.war":
		command => "/usr/bin/unzip /var/lib/tomcat7/webapps/geoserver.war 'data/*'",
		cwd => "${geoserver_data_dir}",
		creates => "${geoserver_data_dir}/data/global.xml",
		require => [File["/var/lib/tomcat7/webapps/geoserver.war"], File["${geoserver_data_dir}"]],
		user => "tomcat7",
	}

	# create geoserver users file with passwords
	file { "${geoserver_data_dir}/security/users.properties" :
		ensure  => file,
		mode => 644,
		owner => "tomcat7",
		group => "tomcat7",
		content => template("geoserver/users.properties.erb"),
		require => File["${geoserver_data_dir}"],
	}

	# create the geoserver workspaces directory
	file { "${geoserver_data_dir}/workspaces" :
		ensure  => directory,
		purge => true,
		force => true,
		recurse => true,
		mode => 644,
		owner => tomcat7,
		group => tomcat7,
		source => "puppet:///modules/geoserver/workspaces",
		require => File["${geoserver_data_dir}"],
	}

	# create the tdwg datastore file
	file { "${geoserver_data_dir}/workspaces/tdwg/tdwg/datastore.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		content => template("geoserver/tdwg_datastore.xml.erb"),
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# create the emonocot datastore file
	file { "${geoserver_data_dir}/workspaces/emonocot/gis/datastore.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		content => template("geoserver/emonocot_datastore.xml.erb"),
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the emonocot style definition
	file { "${geoserver_data_dir}/styles/eMonocot.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the emonocot SLD file
	file { "${geoserver_data_dir}/styles/eMonocot.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot1_native style definition
	file { "${geoserver_data_dir}/styles/eMonocot1_native.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot1_native.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot1_native SLD file
	file { "${geoserver_data_dir}/styles/eMonocot1_native.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot1_native.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot1_introduced style definition
	file { "${geoserver_data_dir}/styles/eMonocot1_introduced.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot1_introduced.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot1_introduced SLD file
	file { "${geoserver_data_dir}/styles/eMonocot1_introduced.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot1_introduced.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot2_native style definition
	file { "${geoserver_data_dir}/styles/eMonocot2_native.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot2_native.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot2_native SLD file
	file { "${geoserver_data_dir}/styles/eMonocot2_native.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot2_native.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot2_introduced style definition
	file { "${geoserver_data_dir}/styles/eMonocot2_introduced.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot2_introduced.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot2_introduced SLD file
	file { "${geoserver_data_dir}/styles/eMonocot2_introduced.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot2_introduced.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}
	
	# Install the eMonocot3_native style definition
	file { "${geoserver_data_dir}/styles/eMonocot3_native.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot3_native.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot3_native SLD file
	file { "${geoserver_data_dir}/styles/eMonocot3_native.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot3_native.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot3_introduced style definition
	file { "${geoserver_data_dir}/styles/eMonocot3_introduced.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot3_introduced.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot3_introduced SLD file
	file { "${geoserver_data_dir}/styles/eMonocot3_introduced.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot3_introduced.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot4_native style definition
	file { "${geoserver_data_dir}/styles/eMonocot4_native.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot4_native.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot4_native SLD file
	file { "${geoserver_data_dir}/styles/eMonocot4_native.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot4_native.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot4_introduced style definition
	file { "${geoserver_data_dir}/styles/eMonocot4_introduced.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot4_introduced.xml",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	# Install the eMonocot4_introduced SLD file
	file { "${geoserver_data_dir}/styles/eMonocot4_introduced.sld" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "puppet:///modules/geoserver/eMonocot4_introduced.sld",
		require => File["${geoserver_data_dir}/workspaces"],
	}

	 file { ["/var/lib/tomcat7/webapps/geoserver",
		 "/var/lib/tomcat7/webapps/geoserver/WEB-INF",
		 "/var/lib/tomcat7/webapps/geoserver/WEB-INF/lib/"] :
                ensure  => directory,
                mode => 755,
                owner => "tomcat7",
                group => "tomcat7",
                require => File["/var/lib/tomcat7/webapps/geoserver.war"],
        }


	wget::fetch { 'fetch-geoserver-mysql':
		source      => "http://download.osgeo.org/webdav/geotools/org/geotools/jdbc/gt-jdbc-mysql/9.2/gt-jdbc-mysql-9.2.jar",
		destination => "/var/tmp/gt-jdbc-mysql-9.2.jar",
	} ->
	file { "/var/lib/tomcat7/webapps/geoserver/WEB-INF/lib/gt-jdbc-mysql-9.2.jar" :
		ensure  => present,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "/var/tmp/gt-jdbc-mysql-9.2.jar",
		require => File["/var/lib/tomcat7/webapps/geoserver.war"],
	}

	package { "libmysql-java":
		ensure => "present",
	} ->
	file { "/var/lib/tomcat7/webapps/geoserver/WEB-INF/lib/mysql-connector-java.jar" :
		ensure  => present,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		source => "/usr/share/java/mysql-connector-java-5.1.28.jar",
		require => File["/var/lib/tomcat7/webapps/geoserver.war"],
	}
#	file { "/var/lib/tomcat7/webapps/geoserver/WEB-INF/lib/mysql-connector-java.jar" :
#		ensure  => link,
#		target => "/usr/share/java/mysql-connector-java.jar",
#		require => File["/var/lib/tomcat7/webapps/geoserver.war"],
#	}

	file { "/var/lib/tomcat7/webapps/geoserver/WEB-INF/web.xml" :
		ensure  => file,
		mode => 664,
		owner => "tomcat7",
		group => "tomcat7",
		content => template("geoserver/web.xml.erb"),
		require => File["/var/lib/tomcat7/webapps/geoserver.war"],
	}
}
