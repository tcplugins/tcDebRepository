package debrepo.teamcity.ebean.server;

import io.ebean.EbeanServer;

public interface EbeanServerProvider {

	EbeanServer getEbeanServer();

}