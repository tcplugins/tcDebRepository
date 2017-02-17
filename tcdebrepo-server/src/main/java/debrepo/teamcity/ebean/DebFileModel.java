package debrepo.teamcity.ebean;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.avaje.ebean.Model;
import com.avaje.ebean.annotation.Index;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "o_debfile")
@Getter
@Setter
@Index(columnNames = {"build_id", "filename"})
public class DebFileModel extends Model {

	public static Find<Long, DebFileModel> getFind() {
		return find;
	}

	public static final Find<Long, DebFileModel> find = new Find<Long, DebFileModel>() {
	};

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
	
	@OneToMany(mappedBy = "debFile", cascade=CascadeType.ALL)
	List<DebPackageModel> debpackages;

	private String packageName;

	private String version;

	private String arch;

	private Long buildId;

	private String buildTypeId;

	private String filename;

	@OneToMany(mappedBy = "debFile", fetch=FetchType.EAGER, cascade=CascadeType.ALL)
	private List<DebPackageParameterModel> packageParameters;

	public Map<String,String> getParameters() {
		Map<String, String> map = new TreeMap<>();
		for (DebPackageParameterModel m : packageParameters) {
			map.put(m.getName(), m.getValue());
		}
		return map;
	}
	
	public void setParameters(Map<String,String> parametersMap) {
		packageParameters.clear();
		for (Entry<String,String> e : parametersMap.entrySet()) {
			packageParameters.add(new DebPackageParameterModel(null, this, e.getKey(), e.getValue()));
		}
	}
	
	public boolean isPopulated() {
		return this.arch != null && this.packageName != null && this.version != null;
	}

	public void populateMetadata(Map<String, String> metaData) {
		this.packageParameters.clear();
		this.setParameters(metaData);
		
		if (metaData.containsKey("Package")) {
			this.setPackageName(metaData.get("Package"));
		}
		
		if (metaData.containsKey("Version")) {
			this.setVersion(metaData.get("Version"));
		}
		
		if (metaData.containsKey("Architecture")) {
			this.setArch(metaData.get("Architecture"));
		}
	}
	
	protected void replaceParameter(String key, String newValue) {
		for (DebPackageParameterModel p : getPackageParameters()) {
			if (key.equals(p.getName())) {
				p.setValue(newValue);
			}
		}
	}

	protected void removeParameter(String key) {
		DebPackageParameterModel itemToRemove = null;
		for (DebPackageParameterModel p : getPackageParameters()) {
			if (key.equals(p.getName())) {
				itemToRemove = p;
			}
		}
		if (itemToRemove != null) {
			getPackageParameters().remove(itemToRemove);
		}
	}
}
