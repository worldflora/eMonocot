/**
 * Message Broker  configuration
 */
class messagebroker(
	$broker_port = '61616',
	$max_memory = '512',
	$admin_user = 'admin',
	$emonocot_user = 'emonocot',
	$admin_password,
	$emonocot_password
	) {

	# Install activemq
	package { "activemq":
		ensure => installed,
	}

	file { "/var/run/activemq":
		ensure => "directory",
		owner  => "activemq",
		group  => "activemq",
		mode   => 755,
		require => Package["activemq"],
	}

	file { "/etc/activemq/instances-enabled/main" :
		ensure  => link,
		target => "../instances-available/main",
		notify => Service["activemq"],
		require => Package["activemq"],
	}

	# Following two files may not be needed due to Ubuntu default configuration (above)

	#file { "/etc/activemq/wrapper.conf" :
	#	ensure  => file,
	#	mode => 644,
	#	owner => "root",
	#	group => "root",
	#	content => template("messagebroker/wrapper.conf.erb"),
	#	notify => Service["activemq"],
	#	require => Package["activemq"],
	#}

	file { "/etc/activemq/instances-enabled/main/activemq.xml" :
		ensure  => file,
		mode => 644,
		owner => "root",
		group => "root",
		content => template("messagebroker/activemq.xml.erb"),
		notify => Service["activemq"],
		require => Package["activemq"],
	}

	service { "activemq" :
		enable => true,
		ensure => running,
		require => Package["activemq"],
	}
}
