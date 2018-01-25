class common::mail {
	# Command line utilities
	package { "mailutils":
		ensure => present,
	} ->

	# Postfix (SMTP server)
	package { "postfix":
		ensure => present,
	} ->

	package { "libsasl2-modules":
		ensure => present,
	} ->

	file { "/etc/mailname":
		ensure => present,
		notify => Service['postfix'],
		content => "${container_hostname}\n",
	} ->

	file { "/etc/postfix/sasl_passwd":
		ensure => present,
		notify => Service['postfix'],
		content => '[smtp.sendgrid.net]:2525 WorldFloraSMTP:_tXSGBYFXIcUz0XKE66YwZK_XB8IrEx84RCDxUi1T6UbOCq0',
	} ->

	exec { "sasl_passwd_db":
		path => "/sbin:/usr/sbin",
		command => "postmap /etc/postfix/sasl_passwd",
		creates => "/etc/postfix/sasl_passwd.db",
	} ->

	file { "/etc/postfix/main.cf":
		ensure => present,
		notify => Service['postfix'],
		content => template("common/postfix-main.cf.erb"),
	}

	service { "postfix":
		ensure => running,
		require => Package["postfix"],
	}

	file { "/etc/aliases":
		ensure => present,
		content => "postmaster:	root\nroot:	world-flora-online@googlegroups.com\n",
	}

	exec { 'postalias':
		command => "/usr/sbin/postalias /etc/aliases",
		require => Package['postfix'],
		subscribe => File['/etc/aliases'],
		refreshonly => true,
	}

	# Dovecot (IMAP server)
	package { "dovecot-imapd":
		ensure => present
	} ->

	file { '/etc/dovecot/local.conf':
		ensure => present,
		content => "mail_location = maildir:~/Maildir\n",
	}

#	service { "dovecot":
#		ensure => running,
#		require => Package["dovecot"],
#	}
}
