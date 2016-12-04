package debrepo.teamcity.ebean;

import java.util.List;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_repository")
@Getter
@Setter
public class DebRepositoryModel extends Model {


	public static Find<Long, DebRepositoryModel> getFind() {
		return find;
	}

	public static final Find<Long, DebRepositoryModel> find = new Find<Long, DebRepositoryModel>() {};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	String name;
	
	String uuid;
	
	String projectId;
	
	@OneToMany(mappedBy = "repository")
	List<DebPackageModel> debpackages;
	
}

