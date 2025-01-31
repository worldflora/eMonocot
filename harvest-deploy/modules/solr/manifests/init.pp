/**
 * Solr
 */
class solr (
	$basedir = '/usr/local/solr',
	$logdir = '/var/log/solr',
	$user = 'root',
	$group = 'root',
	$solrconfig = 'solr/solrconfig.xml.erb',
	$master_url = 'set the master url') {

	file { $basedir:
		ensure => directory,
		owner => $user,
		group => $group,
		mode => 0755,
	}

	file { $logdir:
		ensure => directory,
		owner => $user,
		group => $group,
		mode => 0755,
	}

	file { "${basedir}/apache-solr-4.0.0.tgz" :
		ensure  => file,
		mode => 644,
		owner => $user,
		group => $user,
		source => 'puppet:///modules/solr/apache-solr-4.0.0.tar.gz',
		require => File[$basedir],
	}

	exec { "unpack-solr":
		command => "/bin/tar xzf ${basedir}/apache-solr-4.0.0.tgz",
		cwd => $basedir,
		creates => "${basedir}/start.jar",
		require => File["${basedir}/apache-solr-4.0.0.tgz"],
	}

	package { default-jre-headless: }

	exec { "restart-solr":
		command => "/etc/init.d/solr restart",
		require => Exec['unpack-solr'],
	}

	file { "${basedir}/solr-webapp/webapp/WEB-INF/lib/jts-1.11.jar" :
		ensure  => file,
		mode => 644,
		owner => $user,
		group => $user,
		source => 'puppet:///modules/solr/jts-1.11.jar',
		require => Exec['restart-solr'],
	}

	file { "${basedir}/solr/collection1/conf/schema.xml" :
		ensure  => file,
		mode => 644,
		owner => $user,
		group => $user,
		content => template("solr/schema.xml.erb"),
		require => Exec['unpack-solr'],
		before => Service['solr'],
	}

	file { "${basedir}/solr/collection1/conf/solrconfig.xml" :
		ensure  => file,
		mode => 644,
		owner => $user,
		group => $user,
		content => template("solr/solrconfig.xml.erb"),
		require => Exec['unpack-solr'],
		before => Service['solr'],
	}

	file { "${basedir}/solr/collection1/conf/stopwords.txt" :
		ensure  => file,
		mode => 644,
		owner => $user,
		group => $user,
		content => template("solr/stopwords.txt.erb"),
		require => Exec['unpack-solr'],
		before => Service['solr'],
	}

	file { "${basedir}/solr/collection1/conf/mapping-ISOLatin1Accent.txt" :
		ensure  => file,
		mode => 644,
		owner => $user,
		group => $user,
		content => template("solr/mapping-ISOLatin1Accent.txt.erb"),
		require => Exec['unpack-solr'],
		before => Service['solr'],
	}

	file { '/etc/sysconfig':
		ensure => directory,
	}

	file { '/etc/sysconfig/solr':
		ensure => present,
		owner => $user,
		group => $group,
		mode => 0755,
		content => template("solr/sysconfig.erb"),
	}

	file { '/etc/init.d/solr':
		ensure => present,
		owner => $user,
		group => $group,
		mode => 0755,
		content => template("solr/solr.erb"),
	}

	service { 'solr':
		ensure => running,
		enable => true,
		require => [File['/etc/init.d/solr'], File['/etc/sysconfig/solr']]	   
	}
}
