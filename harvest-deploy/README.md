World Flora Portal — Puppet scripts
===================================

These Puppet scripts manage the deployment of the World Flora Portal to the Google Cloud.  They were initially created for use during development, so have some gaps — load balancing, session sharing, database replication etc.

The repository contains some Git submodules, run `./init_submodules.sh` to load them.

[eMonocot](https://github.com/RBGKew/eMonocot)'s configuration is external to the application.  Selecting between eMonocot and the World Flora portal is done using a Java property, added to Tomcat, and using the appropriate archive of static files.

## Deployment to Google's cloud

This roughly follows the steps in https://cloud.google.com/compute/docs/quickstart

### Preparation

- Install `google-cloud-sdk` Ubuntu package on local machine
- Enable Google Compute Engine API on the Developer's Console
- Authenticate to Gcloud with `gcloud auth login`
- Set the default project id `gcloud config set project valiant-hangout-91511`

### Creating an instance

- `gcloud compute images list --project ubuntu-os-cloud --no-standard-images` showed available Ubuntu images
- `gcloud compute instances create wfo-1 --image ubuntu-14-04 --zone europe-west1-d` to create and start an instance.
  I chose europe-west1-d since it's the "latest", but otherwise had no idea how to choose between processor types!
  Output:
````
    Created [https://www.googleapis.com/compute/v1/projects/valiant-hangout-91511/zones/europe-west1-d/instances/wfo-1].
    NAME  ZONE           MACHINE_TYPE  INTERNAL_IP   EXTERNAL_IP   STATUS
    wfo-1 europe-west1-d n1-standard-1 10.240.54.211 104.155.49.79 RUNNING
````
- Allow HTTP access with `gcloud compute firewall-rules create allow-http --allow tcp:80`
- Set up SSH access

### Install tools necessary for using Puppet, Git

- SSH into the machine `ssh wfo-1.europe-west1-d.valiant-hangout-91511`
- `sudo aptitude install git puppet`
- Clone this Git repository
  `git clone git@github.com:worldflora/gc-deploy.git`
  (This will use SSH key forwarding to authenticate, if set up properly.)
- `cd gc-deploy`
- `./init_submodules.sh`
- `sudo puppet apply --show_diff --modulepath $PWD/modules manifests/worldflora.pp`

## Use with Vagrant

Files for use with [Vagrant](https://www.vagrantup.com/) are included.

Some variables in `globals.pp` will need changing — the Ubuntu mirror source (gb, us, etc) and possibly a proxy.

After that, a simple `vagrant up` is all that's required.

## Working around some bugs

Restart SOLR, as something isn't quite right the first time:
  `sudo service solr restart`

And restart Tomcat, likewise
  `sudo service tomcat7 restart`

## Complete

Have a look at the Tomcat manager:
- http://localhost:18080/manager/html (user and password in the script)
- http://104.155.49.79/manager/html (user and password in the script)

Have a look at the portal:
- http://localhost:18080/
- http://104.155.49.79/

Or the harvesting admin UI (developers only!)
- http://localhost:18080/world-flora-harvest (admin/letmein)
- http://104.155.49.79/world-flora-harvest

The portal has no data.  Create a user, then grant it admin privilidges.  That can only be done by adding the entry to the database manually (for the first admin user).  Create an organisation, then add a taxonomic resource, then harvest it.
