package debrepo.teamcity.ebean;

import javax.persistence.Entity;

import com.avaje.ebean.annotation.Sql;

import debrepo.teamcity.service.DebReleaseFileGenerator.DistComponentArchitecture;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Sql @Getter @AllArgsConstructor
public class DebDistCompArchModel implements DistComponentArchitecture {
	
	String dist;
	String component;
	String arch;

}
