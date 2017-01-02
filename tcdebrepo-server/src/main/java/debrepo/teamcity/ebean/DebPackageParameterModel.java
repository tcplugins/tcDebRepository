package debrepo.teamcity.ebean;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.avaje.ebean.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_debpackage_parameter")
@Getter
@Setter @AllArgsConstructor
public class DebPackageParameterModel extends Model {


		public static Find<Long, DebPackageParameterModel> getFind() {
			return find;
		}

		public static final Find<Long, DebPackageParameterModel> find = new Find<Long, DebPackageParameterModel>() {};

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		Long id;
		
		@ManyToOne @Column(name="package")
		private DebPackageModel debPackage;


		private String name;
		private String value;

}
