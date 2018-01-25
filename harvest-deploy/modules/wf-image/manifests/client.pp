 class wf-image::client 
 {
#Install nfs-common package
	package { "nfs-common" :
		ensure => installed
	}

# create Images directory	

	file { "/var/lib/worldflora/images":
		ensure => "directory",
		owner  => "tomcat7",
		group  => "tomcat7",
		mode   => 777,
	}


 #mount on harvester
	 mount { "/var/lib/worldflora/images":
		ensure  => mounted,
		fstype  => "nfs",
		device  => "${image_host}:/var/nfs/images",
		options => "defaults",
		require =>  Package['nfs-common'],
		#require => File['${worldflora_base_dir}/images'],
		#tag     => 'new_mount',
	}

}
