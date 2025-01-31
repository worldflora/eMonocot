class common::apt {
	file { "/etc/timezone":
		content => "UTC\n",
	}

	file { "/etc/default/locale":
		content => "LANG=\"en_GB.utf8\"\nLANGUAGE=\"en_GB:en\"\n",
	}

	exec { 'locale-gen':
		command => "/usr/sbin/locale-gen en_GB.utf8",
		unless => "/usr/bin/locale -a | /bin/grep --quiet en_GB.utf8"
	}

	file { "/etc/localtime":
		ensure => link,
		target => "/usr/share/zoneinfo/UTC",
	}

	file { "/etc/apt/sources.list":
		ensure => present,
		owner => 0,
		group => 0,
		mode => 644,
		content => template("common/sources.list.erb"),
	} ->

	file { "/etc/apt/apt.conf.d/proxy":
		ensure => present,
		owner => 0,
		group => 0,
		mode => 644,
		content => template("common/apt-proxy.erb"),
#		require => File['/etc/apt/sources.list']
	} ->

	exec { 'apt-get update':
		path   => "/usr/bin:/usr/sbin:/bin",
		command => '/usr/bin/apt-get update',
		onlyif => 'test $(date -d Yesterday +%s) -gt $(stat -c %Y /var/lib/apt/lists)',
	}

	# Ensures apt-get update has been run (if necessary) before installing packages
	Exec["apt-get update"] -> Package <| |>
}
