package debrepo.teamcity.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor 
@Data
public class DebRepositoryStatistics {

	Integer totolPackageCount;
	String repositoryUrl;
	
}
