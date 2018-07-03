package ch.teamkoenig.tool.beschriftung;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
@Theme(ValoTheme.THEME_NAME)
@Widgetset("BeschriftungWidgetSet")
public class AppUi extends UI {

	@Autowired
	private SpringViewProvider viewProvider;

	@Override
	protected void init(final VaadinRequest vaadinRequest) {
		final Navigator navigator = new Navigator(this, this);
		navigator.addProvider(viewProvider);
		setNavigator(navigator);
	}

}
