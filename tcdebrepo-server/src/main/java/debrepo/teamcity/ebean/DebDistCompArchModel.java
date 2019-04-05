package debrepo.teamcity.ebean;

import javax.persistence.Entity;

import debrepo.teamcity.entity.DistComponentArchitecture;
import io.ebean.annotation.Sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Entity
@Sql @Getter @AllArgsConstructor
public class DebDistCompArchModel implements DistComponentArchitecture {
	
	String dist;
	String component;
	String arch;

}
