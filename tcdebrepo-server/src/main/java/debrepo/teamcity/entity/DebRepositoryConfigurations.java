package debrepo.teamcity.entity;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

/* Use the XmlAttributes on the fields rather than the getters
 * and setters provided by Lombok */
@XmlAccessorType(XmlAccessType.FIELD)

@Data  // Let Lombok generate the getters and setters.

@XmlRootElement(name="deb-repositories")
public class DebRepositoryConfigurations {
	
	@XmlElement(name="deb-repository-configuration")// @XmlElementWrapper(name="deb-repositories")
	List<DebRepositoryConfiguration> debRepositoryConfigurations = new ArrayList<>();

}
