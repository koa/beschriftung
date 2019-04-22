package ch.teamkoenig.tool.beschriftung.view;

import ch.teamkoenig.tool.beschriftung.layout.*;
import ch.teamkoenig.tool.beschriftung.model.HorseData;
import ch.teamkoenig.tool.beschriftung.model.TeamData;
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
import com.vaadin.ui.*;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.components.grid.Editor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@SpringView
@Title("Labels")
public class TeamListView extends CustomComponent implements View {

  public TeamListView() {

    final ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
    final CollectionType type =
        TypeFactory.defaultInstance().constructCollectionType(ArrayList.class, TeamData.class);
    final ObjectReader reader = objectMapper.readerFor(type);
    final ObjectWriter writer = objectMapper.writerFor(type);

    final List<TeamData> teamList = new ArrayList<>();
    final ListDataProvider<TeamData> teamDataProvider = DataProvider.ofCollection(teamList);
    final AtomicReference<Runnable> updaterRef = new AtomicReference<>(() -> {});
    addAttachListener(
        event -> {
          final ClientStorage clientStorage =
              new ClientStorage(
                  supported -> {
                    if (!supported) {
                      throw new RuntimeException();
                    }
                  });
          clientStorage.setParent(event.getConnector());
          updaterRef.set(
              () -> {
                try {
                  final String valueAsString = writer.writeValueAsString(teamList);
                  clientStorage.setLocalItem("horse-data", valueAsString);
                  teamDataProvider.refreshAll();
                } catch (final JsonProcessingException e1) {
                  log.info("Cannot update data", e1);
                }
              });
          clientStorage.getLocalItem(
              "horse-data",
              value -> {
                if (value == null) return;
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

    final AtomicReference<TeamData> originalTeamData = new AtomicReference<>(null);
    final AtomicReference<TeamData.TeamDataBuilder> currentTeamData = new AtomicReference<>(null);
    editor.addOpenListener(
        openEvent -> {
          final TeamData bean = openEvent.getBean();
          originalTeamData.set(bean);
          currentTeamData.set(bean.toBuilder());
        });
    editor.addSaveListener(
        saveEvent -> {
          final TeamData b = originalTeamData.get();
          teamList.replaceAll(t -> t == b ? currentTeamData.get().build() : t);
          updater.run();
        });
    final Column<TeamData, Boolean> activeColumn =
        teamGrid.addColumn(TeamData::isActive, b -> b ? "x" : "");
    activeColumn.setCaption("Active");
    activeColumn.setEditorComponent(new CheckBox(), (b, v) -> currentTeamData.get().active(v));
    final Column<TeamData, String> nameColumn = teamGrid.addColumn(TeamData::getTeamName);
    nameColumn.setCaption("Name");
    nameColumn.setEditorComponent(new TextField(), (b, v) -> currentTeamData.get().teamName(v));
    final Column<TeamData, String> numberColumn = teamGrid.addColumn(TeamData::getTeamCode);
    numberColumn.setCaption("Nummer");
    numberColumn.setEditorComponent(new TextField(), (b, v) -> currentTeamData.get().teamCode(v));

    final Column<TeamData, Integer> bigCountColumn =
        teamGrid.addColumn(TeamData::getBigWagonNumberCount);
    bigCountColumn.setCaption("Big Numbers");
    bigCountColumn.setEditorComponent(
        createCountComboBox(), (b, v) -> currentTeamData.get().bigWagonNumberCount(v));

    final Column<TeamData, Integer> smallCountColumn =
        teamGrid.addColumn(TeamData::getSmallWagonNumberCount);
    smallCountColumn.setCaption("Small Numbers");
    smallCountColumn.setEditorComponent(
        createCountComboBox(), (b, v) -> currentTeamData.get().smallWagonNumberCount(v));

    editor.setEnabled(true);

    final HorizontalLayout buttonLayout = new HorizontalLayout();

    final Button editButton =
        new Button(
            "Edit",
            clickEvent ->
                teamGrid.getSelectedItems().stream()
                    .findFirst()
                    .ifPresent(
                        selectedTeam -> {
                          final List<HorseData> horsesList = new ArrayList<>();
                          final List<HorseData> horses = selectedTeam.getHorses();
                          if (horses != null) horsesList.addAll(horses);
                          final ListDataProvider<HorseData> horsesDataProvider =
                              DataProvider.ofCollection(horsesList);
                          final Grid<HorseData> horsesGrid = new Grid<>(horsesDataProvider);
                          final AtomicReference<HorseData.HorseDataBuilder> newHorseData =
                              new AtomicReference<>();

                          final Column<HorseData, Boolean> horseActiveColumn =
                              horsesGrid.addColumn(HorseData::isActive, b -> b ? "x" : "");
                          horseActiveColumn.setCaption("Active");
                          horseActiveColumn.setEditorComponent(
                              new CheckBox(), (b, v) -> newHorseData.get().active(v));

                          final Column<HorseData, String> horseNumberColumn =
                              horsesGrid.addColumn(HorseData::getHorseCode);
                          horseNumberColumn.setCaption("Nummer");
                          horseNumberColumn.setEditorComponent(
                              new TextField(), (b, v) -> newHorseData.get().horseCode(v));

                          final Column<HorseData, String> horseNameColumn =
                              horsesGrid.addColumn(HorseData::getHorseName);
                          horseNameColumn.setCaption("Name");
                          horseNameColumn.setEditorComponent(
                              new TextField(), (b, v) -> newHorseData.get().horseName(v));

                          final Column<HorseData, String> horseDetailColumn =
                              horsesGrid.addColumn(HorseData::getHorseDetailText);
                          horseDetailColumn.setCaption("Detail");
                          horseDetailColumn.setEditorComponent(
                              new TextField(), (b, v) -> newHorseData.get().horseDetailText(v));

                          final Column<HorseData, Integer> harnessCountColumn =
                              horsesGrid.addColumn(HorseData::getHarnessNumberCount);
                          harnessCountColumn.setCaption("Harness");
                          harnessCountColumn.setEditorComponent(
                              createCountComboBox(),
                              (b, v) -> newHorseData.get().harnessNumberCount(v));

                          final Column<HorseData, Integer> headCountColumn =
                              horsesGrid.addColumn(HorseData::getHeadNumberCount);
                          headCountColumn.setCaption("Head Numbers");
                          headCountColumn.setEditorComponent(
                              createCountComboBox(),
                              (b, v) -> newHorseData.get().headNumberCount(v));

                          final AtomicReference<HorseData> oldHorseData = new AtomicReference<>();
                          final Editor<HorseData> horseEditor = horsesGrid.getEditor();
                          horseEditor.addOpenListener(
                              openEvent -> {
                                final HorseData bean = openEvent.getBean();
                                oldHorseData.set(bean);
                                newHorseData.set(bean.toBuilder());
                              });
                          horseEditor.addSaveListener(
                              saveEvent -> {
                                horsesList.replaceAll(
                                    t -> t == oldHorseData.get() ? newHorseData.get().build() : t);
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
                          horseButtonLayout.addComponent(
                              new Button(
                                  "close",
                                  closeClickEvent -> {
                                    teamList.replaceAll(
                                        t ->
                                            t == selectedTeam
                                                ? selectedTeam
                                                    .toBuilder()
                                                    .horses(
                                                        Collections.unmodifiableList(horsesList))
                                                    .build()
                                                : t);
                                    updater.run();
                                    editHorsesWindow.close();
                                  }));

                          final Button addButton = new Button("add");
                          addButton.addClickListener(
                              addClickEvent -> {
                                final HorseData.HorseDataBuilder horseDataBuilder =
                                    HorseData.builder().harnessNumberCount(1).headNumberCount(1);
                                final String teamCode = selectedTeam.getTeamCode();
                                for (int i = 0; i < 26; i++) {
                                  String horseCodeProposal = teamCode + ((char) ('A' + i));
                                  if (horsesList.stream()
                                      .noneMatch(h -> h.getHorseCode().equals(horseCodeProposal))) {
                                    horseDataBuilder.horseCode(horseCodeProposal);
                                    break;
                                  }
                                }
                                horsesList.add(horseDataBuilder.build());
                                horsesDataProvider.refreshAll();
                              });

                          horseButtonLayout.addComponent(addButton);
                          final Button deleteButton = new Button("delete");
                          deleteButton.addClickListener(
                              deleteClickEvent -> {
                                horsesList.removeAll(horsesGrid.getSelectedItems());
                                horsesDataProvider.refreshAll();
                              });
                          deleteButton.setVisible(false);
                          horseButtonLayout.addComponent(deleteButton);
                          horsesGrid.addSelectionListener(
                              selectionEvent -> {
                                final boolean isSelected =
                                    selectionEvent.getFirstSelectedItem().isPresent();
                                deleteButton.setVisible(isSelected);
                              });

                          editHorsesWindow.setContent(rootLayout);

                          editHorsesWindow.center();
                          clickEvent.getConnector().getUI().addWindow(editHorsesWindow);
                        }));
    editButton.setVisible(false);

    final Button deleteButton = new Button("Delete");
    deleteButton.addClickListener(
        clickEvent -> {
          teamList.removeAll(teamGrid.getSelectedItems());
          updater.run();
        });
    deleteButton.setVisible(false);

    teamGrid.addSelectionListener(
        selectEvent -> {
          final boolean isSelected = selectEvent.getFirstSelectedItem().isPresent();
          editButton.setVisible(isSelected);
          deleteButton.setVisible(isSelected);
        });

    buttonLayout.addComponent(editButton);
    buttonLayout.addComponent(deleteButton);
    final Button addButton = new Button("Add");
    addButton.addClickListener(
        clickEvent -> {
          teamList.add(TeamData.builder().build());
          updater.run();
        });
    buttonLayout.addComponent(addButton);

    final Button printButton = new Button("Print");

    final StreamResource pdfResource =
        new StreamResource(
            (StreamSource)
                () -> {
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
                                drawables.add(
                                    new HarnessNumberLayout(
                                        horseData.getHorseCode(),
                                        horseData.getHorseName(),
                                        horseData.getHorseDetailText()));
                              }
                              for (int i = 0; i < horseData.getHeadNumberCount(); i++) {
                                drawables.add(
                                    new HeadNumberLayout(
                                        horseData.getHorseCode(),
                                        horseData.getHorseName(),
                                        horseData.getHorseDetailText()));
                              }
                            }
                          }
                        }
                      }
                    }
                    final ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
                    if (!drawables.isEmpty()) {
                      Layouter.layout(
                          drawables, arrayOutputStream, PageSize.A4.rotate(), 5 * 72 / 25.4f);
                    }
                    return new ByteArrayInputStream(arrayOutputStream.toByteArray());
                  } catch (final IOException e) {
                    throw new RuntimeException("Error creating pdf", e);
                  }
                },
            "labels.pdf");
    pdfResource.setCacheTime(0);
    final FileDownloader fileDownloader = new FileDownloader(pdfResource);
    fileDownloader.setOverrideContentType(false);
    fileDownloader.extend(printButton);

    buttonLayout.addComponent(printButton);

    final Button exportButton = new Button("Export");
    final StreamResource resource =
        new StreamResource(
            (StreamSource)
                () -> {
                  try {
                    return new ByteArrayInputStream(writer.writeValueAsBytes(teamList));
                  } catch (final IOException e) {
                    throw new RuntimeException("Error creating export json", e);
                  }
                },
            "teams.json");
    resource.setCacheTime(0);
    final FileDownloader exportDownloader = new FileDownloader(resource);
    exportDownloader.setOverrideContentType(false);
    exportDownloader.extend(exportButton);

    buttonLayout.addComponent(exportButton);

    final Upload importButton =
        new Upload(
            "Import",
            new Upload.Receiver() {
              @Override
              public OutputStream receiveUpload(final String filename, final String mimeType) {
                return new OutputStream() {
                  private final ByteArrayOutputStream stream = new ByteArrayOutputStream();

                  @Override
                  public void write(final int b) {
                    stream.write(b);
                  }

                  @Override
                  public void write(final byte[] b, final int off, final int len) {
                    stream.write(b, off, len);
                  }

                  public void writeTo(final OutputStream out) throws IOException {
                    stream.writeTo(out);
                  }

                  @Override
                  public void close() throws IOException {
                    getUI()
                        .access(
                            () -> {
                              try {
                                final List<TeamData> readValue =
                                    reader.readValue(stream.toByteArray());
                                teamList.clear();
                                teamList.addAll(readValue);
                                teamDataProvider.refreshAll();
                                updaterRef.get().run();
                              } catch (final IOException e) {
                                log.info("Cannot parse data", e);
                              }
                            });
                    super.close();
                  }
                };
              }
            });
    buttonLayout.addComponent(importButton);

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
  public void enter(final ViewChangeEvent event) {}
}
