package ch.teamkoenig.tool.beschriftung.view;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

@SpringView(name = "")
public class EmptyView extends CustomComponent implements View {

	public EmptyView() {

		final Label label = new Label("Test");
		setCompositionRoot(label);

	}

	@Override
	public void enter(final ViewChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
