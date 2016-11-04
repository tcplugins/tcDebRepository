package debrepo.teamcity.web;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

public class DebRepoErrorView {

	@NotNull
	public static ModelAndView createTextView(@NotNull final String message) {
		final ModelAndView mv = new ModelAndView("/simpleView.jsp");
		mv.getModel().put("message", message);
		return mv;
	}

}