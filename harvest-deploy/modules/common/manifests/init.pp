# Common setup for all hosts
 
class common {
	$http_proxy = ""

	require common::apt
	require common::mail

	# Disable the Puppet daemon
	service { "puppet":
		ensure => stopped,
	}

	user { "mb10kg":
		shell => "/bin/zsh",
		require => Package['zsh'],
	}

	file { "/home/mb10kg":
		ensure => directory,
	}

	# This will be on the mb10kg user's $PATH
	file { "/home/mb10kg/bin":
		ensure => directory,
		owner => mb10kg,
		group => mb10kg,
		mode => 755,
	}

	file { "/home/mb10kg/.zshenv":
		ensure => present,
		owner => mb10kg,
		group => mb10kg,
		mode => 644,
		source => "puppet:///modules/common/zshenv",
	}

	file { "/home/mb10kg/.zshrc":
		ensure => present,
		owner => mb10kg,
		group => mb10kg,
		mode => 644,
		source => "puppet:///modules/common/zshrc",
	}
	
	$extrapackages = [
		"curl",
		"emacs24-nox",
		"pv",
		"tree",
		"unzip",
		"zsh",
	]

	package {
		$extrapackages: ensure => installed
	}

	# Keyboard layout
	#console-data
	#keyboard-configuration

	file { "/home/mb10kg/.my.cnf":
		content => "[client]\ndefault-character-set = utf8\n\n[mysql]\nshow-warnings\npager = less -S\n",
	}
}
