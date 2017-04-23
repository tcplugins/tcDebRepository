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
@Entity @Table(name = "o_release_file_simple_hash")
public class DebReleaseFileSimpleHashModel extends Model {
	
	public static Find<Long, DebReleaseFileSimpleHashModel> getFind() {
		return find;
	}

	public static final Find<Long, DebReleaseFileSimpleHashModel> find = new Find<Long, DebReleaseFileSimpleHashModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@ManyToOne
	DebReleaseFileSimpleModel releaseFileSimple; 
	
	String hashType;
	String hashValue;

}
