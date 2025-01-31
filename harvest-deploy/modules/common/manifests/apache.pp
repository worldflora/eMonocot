class common::apache {
	package { "apache2":
		ensure => present
	}

	package { "libapache2-mod-jk":
		ensure => present,
		require => Package["apache2"],
	}

	file { "/etc/apache2/sites-enabled/000-default.conf":
		ensure => absent
	}

	service { "apache2":
		ensure => running,
		require => Package["libapache2-mod-jk"],
	}
}
