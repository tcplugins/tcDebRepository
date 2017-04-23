package debrepo.teamcity.ebean;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity @Table(name = "o_packages_file_hash")
public class DebPackagesFileHashModel extends Model {
	
	public static Find<Long, DebPackagesFileHashModel> getFind() {
		return find;
	}

	public static final Find<Long, DebPackagesFileHashModel> find = new Find<Long, DebPackagesFileHashModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne
	DebPackagesFileModel packagesFile; 
	
	String hashType;
	String hashValue;

}
