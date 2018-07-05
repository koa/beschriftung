package ch.teamkoenig.tool.beschriftung;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.navigator.SpringViewProvider;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringUI
@Theme(ValoTheme.THEME_NAME)
@Widgetset("BeschriftungWidgetSet")
public class AppUi extends UI {

	@Autowired
	private SpringViewProvider viewProvider;

	@Override
	protected void init(final VaadinRequest vaadinRequest) {

		final VerticalLayout rootLayout = new VerticalLayout();
		final Panel mainPanel = new Panel();
		final MenuBar mainMenu = new MenuBar();
		rootLayout.addComponent(mainMenu);
		rootLayout.addComponent(mainPanel);
		rootLayout.setExpandRatio(mainPanel, 1);
		rootLayout.setSizeFull();
		setContent(rootLayout);

		final Navigator navigator = new Navigator(this, mainPanel);
		for (final String viewName : viewProvider.getViewNamesForCurrentUI()) {
			final Class<? extends View> viewClass = viewProvider.getView(viewName).getClass();
			final Title titleAnnotation = viewClass.getAnnotation(Title.class);
			final String menuLabel;
			if (titleAnnotation != null) {
				menuLabel = titleAnnotation.value();
			} else
				menuLabel = viewName;
			mainMenu.addItem(menuLabel, event -> {
				navigator.navigateTo(viewName);
			});
		}
		navigator.addProvider(viewProvider);
		setNavigator(navigator);
		viewProvider.getViewNamesForCurrentUI().stream().findFirst().ifPresent(navigator::navigateTo);
	}

}
