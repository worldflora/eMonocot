# = Class: tomcat7
# 
# This class installs/configures/manages the Tomcat v7 Java application server.
# Only supported on Debian-derived OSes.
# 
# == Parameters: 
#
# $enable::	 Whether to start the Tomcat service on boot. Defaults to
#			   true. Valid values: true and false. 
# $ensure::	 Whether to run the Tomcat service. Defaults to running.
#			   Valid values: running and stopped. 
# $http_port::  Port to configure the HTTP connector with. Defaults to 8080.
# $https_port:: Port to configure the HTTPS connector with. Defaults to 8443.
# $jre::		JRE package to depend on. Defaults to 'default'. Suggested
#			   values: 'openjdk-6', 'openjdk-7', 'sun-java6', 'oracle-java7'
# $install_admin:: Whether to install the tomcat7-admin package. Defaults to
#				  true. Valid values: true and false.
# 
# == Requires: 
# 
# Nothing.
# 
# == Sample Usage:
#
#   class {'tomcat7':
#	 http_port => 80,
#	 jre => 'openjdk-7',
#   }
#   class {'tomcat7':
#	 enable => false,
#	 ensure => stopped,
#   }
#

class tomcat7 (
	$enable = true,
	$ensure = running,
	$http_port = 8080,
	$https_port = 8443,
	$jre = 'openjdk-7',
	$install_admin = true,
) {
  $jre_package = "${jre}-jre-headless"

  package { 'tomcat7':
	ensure => installed,
	require => [
	  Package[$jre_package],
	  Package['authbind'],
	  Package['libtcnative'],
	],
  }

  if ($install_admin) {
	package { 'tomcat7-admin':
	  ensure => installed,
	  require => Package['tomcat7'],
	}
  }

  package { $jre_package:
  }

  package { 'libtcnative':
	name => 'libtcnative-1',
  }

  package { 'authbind':
  }

  file { '/etc/tomcat7/server.xml':
	 owner => 'root',
	 require => Package['tomcat7'],
	 notify => Service['tomcat7'],
	 content => template('tomcat7/server.xml.erb'),
  }

  exec { 'remove default Tomcat ROOT':
	path => "/bin:/usr/bin",
	command => 'rm -Rf /var/lib/tomcat7/webapps/ROOT',
	onlyif => 'cmp /var/lib/tomcat7/webapps/ROOT/index.html /usr/share/tomcat7-root/default_root/index.html',
	require => Package['tomcat7'],
  }

	# Make sure JDK 7 is the default Java

	exec { 'update-java-alternatives':
		path   => "/usr/bin:/usr/sbin:/bin",
		command => '/usr/sbin/update-java-alternatives --set java-1.7.0-openjdk-i386',
		onlyif => 'test $(java -version 2>&1 | head -n 1 | grep --only-matching "1\..") != 1.7',
	}

	file { "/etc/default/tomcat7":
		ensure => present,
		owner => 0,
		group => 0,
		mode => 644,
		#source => "puppet:///modules/tomcat7/etc/default/tomcat7.erb",
		content => template("tomcat7/tomcat7.erb"),
		require => Package["tomcat7"],
	}

	file { "/etc/tomcat7/tomcat-users.xml":
		ensure => present,
		owner => 0,
		group => tomcat7,
		mode => 640,
		source => "puppet:///modules/tomcat7/etc/tomcat7/tomcat-users.xml",
		require => Package["tomcat7"],
	}

	file { "/etc/tomcat7/web.xml":
		ensure => present,
		owner => 0,
		group => tomcat7,
		mode => 644,
		source => "puppet:///modules/tomcat7/etc/tomcat7/web.xml",
		require => Package["tomcat7"],
	}

	file { "/etc/tomcat7/context.xml":
		ensure => present,
		owner => 0,
		group => tomcat7,
		mode => 644,
		source => "puppet:///modules/tomcat7/etc/tomcat7/context.xml",
		require => Package["tomcat7"],
	}

	# Bug in Ubuntu, see https://bugs.launchpad.net/ubuntu/+source/tomcat7/+bug/1232258
	file { '/usr/share/tomcat7/common':
		ensure => link,
		target => '/var/lib/tomcat7/common',
		require => Package["tomcat7"],
	}

  service { 'tomcat7':
	ensure => $ensure,
	enable => $enable,
	require => Package['tomcat7'],
  }   

}

