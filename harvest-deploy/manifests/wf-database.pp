# World Flora Online Database

import 'globals.pp'

require common

# Solr
require solr

# MySQL
class { 'wf-mysql':
	mysql_password => "mysql",
	geoserver_database => "gis",
	database => "worldflora",
	read_write_user => "worldflora",
	read_write_user_password => "wf",
	geoserver_user => "geoserver",
	geoserver_user_password => "gs",
}
require wf-mysql
