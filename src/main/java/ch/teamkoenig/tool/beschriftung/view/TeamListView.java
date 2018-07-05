package ch.teamkoenig.tool.beschriftung.view;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.wolfie.clientstorage.ClientStorage;
import com.itextpdf.kernel.geom.PageSize;
import com.vaadin.annotations.Title;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.Editor;

import ch.teamkoenig.tool.beschriftung.layout.Drawable;
import ch.teamkoenig.tool.beschriftung.layout.HarnessNumberLayout;
import ch.teamkoenig.tool.beschriftung.layout.HeadNumberLayout;
import ch.teamkoenig.tool.beschriftung.layout.Layouter;
import ch.teamkoenig.tool.beschriftung.layout.WagonNumberLayout;
import ch.teamkoenig.tool.beschriftung.model.HorseData;
import ch.teamkoenig.tool.beschriftung.model.TeamData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringView
@Title("Labels")
public class TeamListView extends CustomComponent implements View {

	public TeamListView() {

		final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
		final CollectionType type = TypeFactory.defaultInstance().constructCollectionType(ArrayList.class,
				TeamData.class);
		final ObjectReader reader = objectMapper.readerFor(type);
		final ObjectWriter writer = objectMapper.writerFor(type);

		final List<TeamData> teamList = new ArrayList<>();
		final ListDataProvider<TeamData> teamDataProvider = DataProvider.ofCollection(teamList);
		final AtomicReference<Runnable> updaterRef = new AtomicReference<Runnable>(() -> {
		});
		addAttachListener(event -> {
			final ClientStorage clientStorage = new ClientStorage(new ClientStorage.ClientStorageSupportListener() {
				@Override
				public void clientStorageIsSupported(final boolean supported) {
					if (!supported) {
						throw new RuntimeException();
					}
				}
			});
			clientStorage.setParent(event.getConnector());
			updaterRef.set(() -> {
				try {
					final String valueAsString = writer.writeValueAsString(teamList);
					clientStorage.setLocalItem("horse-data", valueAsString);
					teamDataProvider.refreshAll();
				} catch (final JsonProcessingException e1) {
					log.info("Cannot update data", e1);
				}
			});
			clientStorage.getLocalItem("horse-data", value -> {
				try {
					final List<TeamData> readValue = reader.readValue(value);
					teamList.clear();
					teamList.addAll(readValue);
					teamDataProvider.refreshAll();
				} catch (final IOException e) {
					log.info("Cannot parse data", e);
				}

			});
		});
		final Runnable updater = () -> updaterRef.get().run();
		final Grid<TeamData> teamGrid = new Grid<>(teamDataProvider);
		final Editor<TeamData> editor = teamGrid.getEditor();

		final AtomicReference<TeamData> originalTeamData = new AtomicReference<TeamData>(null);
		final AtomicReference<TeamData.TeamDataBuilder> currentTeamData = new AtomicReference<>(null);
		editor.addOpenListener(openEvent -> {
			final TeamData bean = openEvent.getBean();
			originalTeamData.set(bean);
			currentTeamData.set(bean.toBuilder());
		});
		editor.addSaveListener(saveEvent -> {
			final TeamData b = originalTeamData.get();
			teamList.replaceAll(t -> t == b ? currentTeamData.get().build() : t);
			updater.run();
		});
		final Column<TeamData, Boolean> activeColumn = teamGrid.addColumn(e -> e.isActive(), b -> b ? "x" : "");
		activeColumn.setCaption("Active");
		activeColumn.setEditorComponent(new CheckBox(), (b, v) -> currentTeamData.get().active(v));
		final Column<TeamData, String> nameColumn = teamGrid.addColumn(e -> e.getTeamName());
		nameColumn.setCaption("Name");
		nameColumn.setEditorComponent(new TextField(), (b, v) -> currentTeamData.get().teamName(v));
		final Column<TeamData, String> numberColumn = teamGrid.addColumn(e -> e.getTeamCode());
		numberColumn.setCaption("Nummer");
		numberColumn.setEditorComponent(new TextField(), (b, v) -> currentTeamData.get().teamCode(v));

		final Column<TeamData, Integer> bigCountColumn = teamGrid.addColumn(e -> e.getBigWagonNumberCount());
		bigCountColumn.setCaption("Big Numbers");
		bigCountColumn.setEditorComponent(createCountComboBox(),
				(b, v) -> currentTeamData.get().bigWagonNumberCount(v));

		final Column<TeamData, Integer> smallCountColumn = teamGrid.addColumn(e -> e.getSmallWagonNumberCount());
		smallCountColumn.setCaption("Small Numbers");
		smallCountColumn.setEditorComponent(createCountComboBox(),
				(b, v) -> currentTeamData.get().smallWagonNumberCount(v));

		editor.setEnabled(true);

		final HorizontalLayout buttonLayout = new HorizontalLayout();

		final Button editButton = new Button("Edit", clickEvent -> {
			teamGrid.getSelectedItems().stream().findFirst().ifPresent(selectedTeam -> {
				final List<HorseData> horsesList = new ArrayList<>();
				final List<HorseData> horses = selectedTeam.getHorses();
				if (horses != null)
					horsesList.addAll(horses);
				final ListDataProvider<HorseData> horsesDataProvider = DataProvider.ofCollection(horsesList);
				final Grid<HorseData> horsesGrid = new Grid<>(horsesDataProvider);
				final AtomicReference<HorseData.HorseDataBuilder> newHorseData = new AtomicReference<>();

				final Column<HorseData, Boolean> horseActiveColumn = horsesGrid.addColumn(e -> e.isActive(),
						b -> b ? "x" : "");
				horseActiveColumn.setCaption("Active");
				horseActiveColumn.setEditorComponent(new CheckBox(), (b, v) -> newHorseData.get().active(v));

				final Column<HorseData, String> horseNumberColumn = horsesGrid.addColumn(e -> e.getHorseCode());
				horseNumberColumn.setCaption("Nummer");
				horseNumberColumn.setEditorComponent(new TextField(), (b, v) -> newHorseData.get().horseCode(v));

				final Column<HorseData, String> horseNameColumn = horsesGrid.addColumn(e -> e.getHorseName());
				horseNameColumn.setCaption("Name");
				horseNameColumn.setEditorComponent(new TextField(), (b, v) -> newHorseData.get().horseName(v));

				final Column<HorseData, String> horseDetailColumn = horsesGrid.addColumn(e -> e.getHorseDetailText());
				horseDetailColumn.setCaption("Detail");
				horseDetailColumn.setEditorComponent(new TextField(), (b, v) -> newHorseData.get().horseDetailText(v));

				final Column<HorseData, Integer> harnessCountColumn = horsesGrid
						.addColumn(e -> e.getHarnessNumberCount());
				harnessCountColumn.setCaption("Harness");
				harnessCountColumn.setEditorComponent(createCountComboBox(),
						(b, v) -> newHorseData.get().harnessNumberCount(v));

				final Column<HorseData, Integer> headCountColumn = horsesGrid.addColumn(e -> e.getHeadNumberCount());
				headCountColumn.setCaption("Head Numbers");
				headCountColumn.setEditorComponent(createCountComboBox(),
						(b, v) -> newHorseData.get().headNumberCount(v));

				final AtomicReference<HorseData> oldHorseData = new AtomicReference<>();
				final Editor<HorseData> horseEditor = horsesGrid.getEditor();
				horseEditor.addOpenListener(openEvent -> {
					final HorseData bean = openEvent.getBean();
					oldHorseData.set(bean);
					newHorseData.set(bean.toBuilder());
				});
				horseEditor.addSaveListener(saveEvent -> {
					horsesList.replaceAll(t -> t == oldHorseData.get() ? newHorseData.get().build() : t);
					horsesDataProvider.refreshAll();
					horsesGrid.recalculateColumnWidths();
				});
				horseEditor.setEnabled(true);

				horsesGrid.setWidth(40, Unit.EM);
				horsesGrid.setHeightByRows(4);

				final Window editHorsesWindow = new Window();
				final VerticalLayout rootLayout = new VerticalLayout();
				rootLayout.addComponent(horsesGrid);

				final HorizontalLayout horseButtonLayout = new HorizontalLayout();

				rootLayout.addComponent(horseButtonLayout);
				horseButtonLayout.addComponent(new Button("close", closeClickEvent -> {
					teamList.replaceAll(t -> t == selectedTeam
							? selectedTeam.toBuilder().horses(Collections.unmodifiableList(horsesList)).build()
							: t);
					updater.run();
					editHorsesWindow.close();
				}));

				final Button addButton = new Button("add");
				addButton.addClickListener(addClickEvent -> {
					horsesList.add(HorseData.builder().build());
					horsesDataProvider.refreshAll();
				});

				horseButtonLayout.addComponent(addButton);
				final Button deleteButton = new Button("delete");
				deleteButton.addClickListener(deleteClickEvent -> {
					horsesList.removeAll(horsesGrid.getSelectedItems());
					horsesDataProvider.refreshAll();
				});
				deleteButton.setVisible(false);
				horseButtonLayout.addComponent(deleteButton);
				horsesGrid.addSelectionListener(selectionEvent -> {
					final boolean isSelected = selectionEvent.getFirstSelectedItem().isPresent();
					deleteButton.setVisible(isSelected);
				});

				editHorsesWindow.setContent(rootLayout);

				editHorsesWindow.center();
				clickEvent.getConnector().getUI().addWindow(editHorsesWindow);
			});
		});
		editButton.setVisible(false);

		final Button deleteButton = new Button("Delete");
		deleteButton.addClickListener(clickEvent -> {
			teamList.removeAll(teamGrid.getSelectedItems());
			updater.run();
		});
		deleteButton.setVisible(false);

		teamGrid.addSelectionListener(selectEvent -> {
			final boolean isSelected = selectEvent.getFirstSelectedItem().isPresent();
			editButton.setVisible(isSelected);
			deleteButton.setVisible(isSelected);
		});

		buttonLayout.addComponent(editButton);
		buttonLayout.addComponent(deleteButton);
		final Button addButton = new Button("Add");
		addButton.addClickListener(clickEvent -> {
			teamList.add(TeamData.builder().build());
			updater.run();
		});
		buttonLayout.addComponent(addButton);

		final Button printButton = new Button("Print");

		final StreamSource source = new StreamSource() {

			@Override
			public InputStream getStream() {
				try {
					final Collection<Drawable> drawables = new ArrayList<>();
					for (final TeamData teamData : teamList) {
						if (teamData.isActive()) {
							for (int i = 0; i < teamData.getBigWagonNumberCount(); i++) {
								drawables.add(new WagonNumberLayout(teamData.getTeamCode(), true));
							}
							for (int i = 0; i < teamData.getSmallWagonNumberCount(); i++) {
								drawables.add(new WagonNumberLayout(teamData.getTeamCode(), false));
							}
							final List<HorseData> horses = teamData.getHorses();
							if (horses != null) {
								for (final HorseData horseData : horses) {
									if (horseData.isActive()) {
										for (int i = 0; i < horseData.getHarnessNumberCount(); i++) {
											drawables.add(new HarnessNumberLayout(horseData.getHorseCode(),
													horseData.getHorseName(), horseData.getHorseDetailText()));
										}
										for (int i = 0; i < horseData.getHarnessNumberCount(); i++) {
											drawables.add(new HeadNumberLayout(horseData.getHorseCode(),
													horseData.getHorseName(), horseData.getHorseDetailText()));
										}
									}
								}
							}
						}
					}
					final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
					if (!drawables.isEmpty()) {
						final OutputStream target = arrayOutputStream;
						Layouter.layout(drawables, target, PageSize.A4.rotate(), 5 * 72 / 25.4f);
					}
					return new ByteArrayInputStream(arrayOutputStream.toByteArray());
				} catch (final IOException e) {
					throw new RuntimeException("Error creating pdf", e);
				}
			}
		};
		final StreamResource pdfResource = new StreamResource(source, "labels.pdf");
		final FileDownloader fileDownloader = new FileDownloader(pdfResource);
		fileDownloader.setOverrideContentType(false);
		fileDownloader.extend(printButton);

		buttonLayout.addComponent(printButton);

		final VerticalLayout rootLayout = new VerticalLayout();
		rootLayout.addComponent(teamGrid);
		rootLayout.addComponent(buttonLayout);
		rootLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
		teamGrid.setSizeFull();
		rootLayout.setExpandRatio(teamGrid, 1);
		rootLayout.setSizeFull();

		setCompositionRoot(rootLayout);
		setSizeFull();
	}

	private ComboBox<Integer> createCountComboBox() {
		final ComboBox<Integer> comboBox = new ComboBox<>("", Arrays.asList(0, 1, 2, 3));
		comboBox.setEmptySelectionAllowed(false);
		comboBox.setTextInputAllowed(false);
		return comboBox;
	}

	@Override
	public void enter(final ViewChangeEvent event) {
	}

}
