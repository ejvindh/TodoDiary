/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tododiary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

/**
 *
 * @author eh
 */
public class TodoDiaryMainController implements Initializable {
    
    private boolean viewTypeSingleDay;
    private boolean jumbleView;
    final static private String db_name = "db";
    final static private String jumbleFileName = "jumble.txt";
    private boolean enable_todo;
    private String prev_entrytext = "";
    private String prev_marked_done_by_button_entrytext = "";
    private String date_marker;
    private int count_date_position_in_text;
    String filePath;
    private String db_name_full;
    private final String date_flag = "||||";
    private final String todo_flag_sign = "++++";
    private String todo_flag;
    private final String endline = "\r\n";
    private final String encoding = "UTF-16";
    private int undone_count = -1;
    private boolean marked_todo_in_file = false;
    private boolean entrytext_changed = false;
    private boolean marked_done_by_button = false;
    private boolean entrytext_changed_after_marked_done_by_button = false;
    private ArrayList<Integer> todo_day_of_years = new ArrayList<Integer>();
    private int thisyear;
    private int thisday;
    private int currentyear;
    
    @FXML
    private DatePicker date_picked;
    
    @FXML
    public MenuBar extras_button;
    
    @FXML
    private CheckMenuItem EnableTodo;
    
    @FXML
    private MenuItem ChooseFolder;
    
    @FXML
    private RadioButton singleDayView;
    
    @FXML
    private RadioButton rawDBView;
    
    @FXML
    private Button save_button;
    
    @FXML
    private Button goto_today;
    
    @FXML
    private Button jumble_button;
    
    @FXML
    private Button markdone_button;
    
    @FXML
    private Button prevtodo_button;
    
    @FXML
    private Button nexttodo_button;
    
    @FXML
    private TextArea entrytext;
    
    @FXML
    private Label todocount;
    private Window stage;
    
    @FXML
    private void handleEnableTodoButtonAction(ActionEvent event) {
        enable_todo = !enable_todo;
        if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";
        ButtonTexts();
    }

    @FXML
    private void handleChooseFolderButtonAction(ActionEvent event) {
        save_changed_entrytext();
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Find DB-files");
        File initialDir = new File(filePath);
        File chosenDir;
        if (initialDir.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDir);
        } else {
            directoryChooser.setInitialDirectory(
                    new File(System.getProperty("user.home")));
        }
        chosenDir = directoryChooser.showDialog(stage);
        if (chosenDir != null) {
            filePath = chosenDir.getAbsolutePath();
        }
        LocalDate date = date_picked.getValue();
        int yy = date.getYear();
        int mm = date.getMonthValue()-1;
        int dd = date.getDayOfMonth();
        populateText(yy, mm, dd);
        ButtonTexts();
    }

    @FXML
    private void handleSingleDayButtonAction(ActionEvent event) {
        if (!viewTypeSingleDay) { //Hvis den var sat i forvejen, så gør intet
            if (!jumbleView) save_changed_entrytext();
            viewTypeSingleDay = true;
            if (!jumbleView) {
                LocalDate date = date_picked.getValue();
                int yy = date.getYear();
                int mm = date.getMonthValue()-1;
                int dd = date.getDayOfMonth();
                populateText(yy, mm, dd);
            }
        }
        ButtonTexts();
    }

    @FXML
    private void handleRawDBButtonAction(ActionEvent event) {
        if (viewTypeSingleDay) { //Hvis den var sat i forvejen, så gør intet
            if (!jumbleView) save_changed_entrytext();
            viewTypeSingleDay = false;
            if (!jumbleView) {// Hvis jumbleview, gøres foreløbig ikke mere
                LocalDate date = date_picked.getValue();
                int yy = date.getYear();
                int mm = date.getMonthValue()-1;
                int dd = date.getDayOfMonth();
                populateText(yy, mm, dd);
            }
        }
        ButtonTexts();
    }
    
    @FXML
    private void handleSaveButtonAction(ActionEvent event) {
        save_changed_entrytext();
	undone_count = countUndone(currentyear);
        ButtonTexts();
    }
    
    @FXML
    private void handleTodayButtonAction(ActionEvent event) {
        save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
        if (currentyear != thisyear) {
                change_year(thisyear);
        }
        currentyear = thisyear;
        setPickedDayOfYear(thisday);
    }
    
    @FXML
    private void handleJumbleButtonAction(ActionEvent event) {
        save_changed_entrytext();
        jumbleView = !jumbleView;
        LocalDate date = date_picked.getValue();
        int yy = date.getYear();
        int mm = date.getMonthValue()-1;
        int dd = date.getDayOfMonth();
        populateText(yy, mm, dd);//Ved JumbleView bruges date dog slet ikke
        ButtonTexts();
    }
    
    @FXML
    private void handleMarkDoneButtonAction(ActionEvent event) {
        // Mark-done button
        marked_done_by_button = true;
        entrytext_changed_after_marked_done_by_button = false;
        prev_marked_done_by_button_entrytext = entrytext.getText();
        ButtonTexts();

    }
    
    @FXML
    private void handlePrevTodoButtonAction(ActionEvent event) {
        //prevtodo-button
        int selected_day_of_year = get_selected_day_of_year();
        int prev_day=0;
        for (int parsed_days : todo_day_of_years) {
            if (parsed_days<selected_day_of_year) prev_day=parsed_days;
        }
        if (prev_day !=0) {
            setPickedDayOfYear(prev_day);
        }
        ButtonTexts();
    }
    
    @FXML
    private void handleNextTodoButtonAction(ActionEvent event) {
        //nexttodo-button
        int selected_day_of_year = get_selected_day_of_year();
        int next_day=0;
        for (int parsed_days : todo_day_of_years) {
            if (next_day==0 && parsed_days>selected_day_of_year) next_day=parsed_days;
        }
        if (next_day !=0) {
            setPickedDayOfYear(next_day);
        }
        ButtonTexts();
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Åben for gemte indstillinger fra forrige kørsel
        Preferences userPrefs = Preferences.userNodeForPackage(getClass());
        singleDayView.setText("Single-Day-View");
        viewTypeSingleDay = userPrefs.getBoolean("singleDayView", true);
        jumbleView = userPrefs.getBoolean("jumbleView", false);
        singleDayView.setSelected(viewTypeSingleDay);
        rawDBView.setText("Raw DB View");
        rawDBView.setSelected(!viewTypeSingleDay);
        EnableTodo.setText("Enable Todo");
        enable_todo = userPrefs.getBoolean("EnableTodo", false);
        EnableTodo.setSelected(enable_todo);
        save_button.setText("Save");
        goto_today.setText("Today");
        prevtodo_button.setText("Prev Todo");
        nexttodo_button.setText("Next Todo");
        if (enable_todo) todo_flag = todo_flag_sign; else todo_flag = "";
        LocalDate dt;
        dt = LocalDate.now();
        date_picked.setValue(dt);
        thisday = dt.getDayOfYear();
        thisyear = dt.getYear();
        currentyear = thisyear;
        Calendar selected_date = Calendar.getInstance();
        int selected_day = selected_date.get(Calendar.DAY_OF_MONTH);
        int selected_month = selected_date.get(Calendar.MONTH);
        
        ChooseFolder.setText("Choose Folder");
        File homefolder;
        homefolder = new File(System.getProperty("user.home"));
        filePath = userPrefs.get("filePath", homefolder.getAbsolutePath());
        setDBNames(currentyear);//hent databasefilernes navn udfra årstal
        createDBFileIfNotExists();
        populateText(currentyear, selected_month, selected_day);
        undone_count = countUndone(currentyear);//Hvor mange undone's er der i år?
        ButtonTexts();//opdater knapper o.lign
        
        
        date_picked.valueProperty().addListener((ObservableValue<? extends LocalDate> observable, LocalDate oldValue, LocalDate newValue) -> {
            entrytext.setDisable(false);
            save_changed_entrytext();//Gem teksten, hvis den er blevet ændret
            int yy = newValue.getYear();
            int mm = newValue.getMonthValue()-1;
            int dd = newValue.getDayOfMonth();
            if (yy!=currentyear) {
                //Har der været årsskift i Datepicker?
                change_year(yy);
            }
            undone_count = countUndone(currentyear);
            ButtonTexts();
            populateText(yy, mm, dd);
        });

        entrytext.textProperty().addListener((ObservableValue<? extends String> observableValue, String s, String s2) -> {
            entrytext_changed = entrytext.getText().compareTo(prev_entrytext) != 0;
            entrytext_changed_after_marked_done_by_button = entrytext.getText().compareTo(prev_marked_done_by_button_entrytext) != 0;
            ButtonTexts();
        });

        Platform.runLater(new Runnable() {

            public void run() {
                date_picked.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN),
                        date_picked::show);
                extras_button.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN),
                        extras_button::requestFocus);
                singleDayView.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN),
                        singleDayView::fire);
                rawDBView.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN),
                        rawDBView::fire);
                save_button.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN),
                        save_button::fire);
                goto_today.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN),
                        goto_today::fire);
                jumble_button.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN),
                        jumble_button::fire);
                markdone_button.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN),
                        markdone_button::fire);
                prevtodo_button.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN),
                        prevtodo_button::fire);
                nexttodo_button.getScene().getAccelerators().put(
                        new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN),
                        nexttodo_button::fire);
            }
        });

        date_picked.setTooltip(new Tooltip("<ctrl/cmd d>"));
        extras_button.setTooltip(new Tooltip("<ctrl/cmd e>"));
        singleDayView.setTooltip(new Tooltip("<ctrl/cmd o>"));
        rawDBView.setTooltip(new Tooltip("<ctrl/cmd r>"));
        save_button.setTooltip(new Tooltip("<ctrl/cmd s>"));
        goto_today.setTooltip(new Tooltip("<ctrl/cmd t>"));
        jumble_button.setTooltip(new Tooltip("<ctrl/cmd j>"));
        markdone_button.setTooltip(new Tooltip("<ctrl/cmd m>"));
        prevtodo_button.setTooltip(new Tooltip("<ctrl/cmd p>"));
        nexttodo_button.setTooltip(new Tooltip("<ctrl/cmd n>"));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Preferences userPrefs1 = Preferences.userNodeForPackage(getClass());
            userPrefs1.putBoolean("singleDayView", viewTypeSingleDay);
            userPrefs1.putBoolean("jumbleView", jumbleView);
            userPrefs1.putBoolean("EnableTodo", enable_todo);
            userPrefs1.put("filePath", filePath);
            save_changed_entrytext();
        }));
    }

    public void ButtonTexts() {
        //Opdatere udseendet af de forskellige knapper o.lign.
        
        //*************** Datepicker
        date_picked.setDisable(jumbleView);//Slukket i Jumbleview
        
        //*************** SingleDay/RawDB-view
        singleDayView.setSelected(viewTypeSingleDay);
        rawDBView.setSelected(!viewTypeSingleDay);
        
        
        //*************** SaveButton
        save_button.setDisable(!(entrytext_changed || marked_done_by_button));
    
        //*************** TodayButton
        //Grey hvis vi allerede er på idag
        int current_day_of_year = get_selected_day_of_year();
        goto_today.setDisable((current_day_of_year == thisday && currentyear==thisyear));

        //*************** JumbleButton
        if (jumbleView) {
            jumble_button.setText("Calendar");
        } else {
            jumble_button.setText("Jumble");
        }
        
        //****************** Todo-knapperiet: Skal kun vises, hvis enable_todo
        if (enable_todo) {
            //*************** MarkDoneButton
            markdone_button.setVisible(true);
            if ((marked_todo_in_file && !marked_done_by_button) || entrytext_changed_after_marked_done_by_button) {
                markdone_button.setText("Mark Done");
                markdone_button.setTextFill(Color.rgb(128,0,0));
                markdone_button.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
            } else if (marked_done_by_button && !entrytext_changed_after_marked_done_by_button && marked_todo_in_file
                            || marked_done_by_button && !entrytext_changed_after_marked_done_by_button && entrytext_changed) {
                markdone_button.setText("Done");
                markdone_button.setTextFill(Color.rgb(0,107,0));
                markdone_button.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
            } else {
                markdone_button.setText("All set");
                markdone_button.setTextFill(Color.rgb(179,179,179));
                markdone_button.setDisable(true);
            }

            //*************** PrevButton + NextButton
            prevtodo_button.setVisible(true);
            prevtodo_button.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
            nexttodo_button.setVisible(true);
            nexttodo_button.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
            int selected_day_of_year = get_selected_day_of_year();
            if (todo_day_of_years.isEmpty()) {
                prevtodo_button.setDisable(true);
                nexttodo_button.setDisable(true);
            } else {
                if (selected_day_of_year > todo_day_of_years.get(0)) {
                    prevtodo_button.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
                } else {
                    prevtodo_button.setDisable(true);
                }
                if (selected_day_of_year < todo_day_of_years.get(todo_day_of_years.size()-1)) {
                    nexttodo_button.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
                } else {
                    nexttodo_button.setDisable(true);
                }
            }

            //*************** Todo-counter
            todocount.setVisible(true);
            if (undone_count!=-1) {
                if (currentyear<=thisyear) {
                    todocount.setDisable(!(viewTypeSingleDay & !jumbleView));//Kun tændt i SingleDay
                    int temp_counter = undone_count;
                    int currentday = get_selected_day_of_year();
                    if ((currentyear<thisyear) || (currentday < thisday)) {
                            if (marked_todo_in_file && marked_done_by_button) {
                                    temp_counter = temp_counter - 1;
                            }
                    }
                    todocount.setText("Leftovers "+Integer.toString(currentyear)+
                                    ":\n"+Integer.toString(temp_counter)+" entries");
                } else {
                    todocount.setDisable(true);
                    todocount.setText("Leftovers "+Integer.toString(currentyear)+
                                    ":\n------");
                }
            }
        } else {
            //Todo-knapperne skjules, hvis todo-funktionaliteten er slået fra
            markdone_button.setVisible(false);
            prevtodo_button.setVisible(false);
            nexttodo_button.setVisible(false);
            todocount.setVisible(false);
        }
    }

    private void setDBNames(int year) {
        //lave en streng med database-filnavnet ud fra det valgte Ã¥rstal
        db_name_full = db_name+year+".txt";    
    }

    private void WriteDB(int year, String db_name, String date_flag, 
            String endline, String encoding, String filePath) {
        try {
            File dbFile = new File(filePath + "/"+ db_name);
            if(!dbFile.exists()) {
                dbFile.createNewFile();
            }
            try (BufferedWriter bw = new BufferedWriter
                    (new OutputStreamWriter(new FileOutputStream(dbFile), encoding))) {
                if (year == 0) {
                    bw.write("New Jumble-file" + endline);
                } else {
                    String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
                    String parseDayName;
                    Calendar parseDays = Calendar.getInstance();
                    parseDays.set(Calendar.YEAR, year);
                    Integer DayOfYear = 1;
                    parseDays.set(Calendar.DAY_OF_YEAR, DayOfYear);
                    while (parseDays.get(Calendar.YEAR)==year) {
                        parseDayName = dayNames[parseDays.get(Calendar.DAY_OF_WEEK)-1];
                        bw.write(date_flag + " "
                                + parseDayName + " "
                                + parseDays.get(Calendar.YEAR) + "-"
                                + String.format("%02d", (parseDays.get(Calendar.MONTH)+1)) + "-"
                                + String.format("%02d", parseDays.get(Calendar.DAY_OF_MONTH)) +
                                endline);
                        bw.write(endline);
                        DayOfYear++;
                        parseDays.set(Calendar.DAY_OF_YEAR, DayOfYear);
                    }
                }
                bw.flush();
            }
        } catch (IOException ioe) {
        }
    }

    private int countUndone(int currentyear) {
        // Tæl hvor mange undone-markeringer der er i den valgte årgang.
        // Der tælles kun datoer, der ligger før "i dag"
        int counting = 0;
        todo_day_of_years.clear();
        File file = new File(filePath, db_name_full);
        try {
            try (BufferedReader br = new BufferedReader 
                                    (new InputStreamReader(new FileInputStream(file), encoding))) {
                String thisLine;
                if (currentyear < thisyear) {
                    while ((thisLine = br.readLine()) != null) {
                        if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                            todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
                            counting++;
                        }
                    }
                } else if (currentyear == thisyear) {
                    int currentday;
                    while ((thisLine = br.readLine()) != null) {
                        if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                            currentday = convertDateMarkerToDayOfYear(thisLine);
                            todo_day_of_years.add(currentday);
                            if (currentday <=thisday) {
                                counting++;
                            }
                        }
                    }
                } else {
                    while ((thisLine = br.readLine()) != null) {
                        if (thisLine.contains(todo_flag_sign) && thisLine.contains(date_flag)) {
                            todo_day_of_years.add(convertDateMarkerToDayOfYear(thisLine));
                        }
                    }
                }
            }
        } catch (UnsupportedEncodingException e1) {
        } catch (FileNotFoundException e1) {
        } catch (IOException e) {
        }
        return counting;
    }

    private Integer convertDateMarkerToDayOfYear(String thisLine) {
        // Udregne hvilken dag-på-året som den pågældende date-marker repræsenterer
        int parsedmonth;
        int parsedday;
        Calendar parseDays = Calendar.getInstance();
	    parseDays.set(Calendar.YEAR, currentyear);
		
        String pmonth = thisLine.substring(
                        thisLine.length()-(todo_flag_sign.length()+5), 
                        thisLine.length()-(todo_flag_sign.length()+3));
        try {
                parsedmonth = Integer.parseInt(pmonth)-1;
        } catch(NumberFormatException nfe) {
                parsedmonth = 0;
        }
        String pday = thisLine.substring(
                        thisLine.length()-(todo_flag_sign.length()+2), 
                        thisLine.length()-todo_flag_sign.length());
        try {
                parsedday = Integer.parseInt(pday);
        } catch(NumberFormatException nfe) {
                parsedday = 1;
        }
        parseDays.set(Calendar.MONTH, parsedmonth);
        parseDays.set(Calendar.DAY_OF_MONTH, parsedday);
        return parseDays.get(Calendar.DAY_OF_YEAR);
    }

    private int get_selected_day_of_year() {
        // Udregne hvilken day-of-year den valgte dato udgør
        LocalDate date = date_picked.getValue(); 
        return date.getDayOfYear();
    }

    private void populateText(int yy, int mm, int dd) {
        // fyld indhold i entrytext
        String entry_txt;
        entry_txt = "";
        if (jumbleView) { //Vis indholdet af jumblefile
            createDBFileIfNotExists();
            File jumblefile = new File(filePath, jumbleFileName);
            try {
                StringBuffer jumbletxt;
                try (BufferedReader jumble_br = new BufferedReader 
                                        (new InputStreamReader(new FileInputStream(jumblefile), encoding))) {
                    jumbletxt = new StringBuffer();
                    String thisLine;
                    while ((thisLine = jumble_br.readLine()) != null) {
                        jumbletxt.append(thisLine).append("\n");
                    }
                }
                entry_txt = jumbletxt.toString();
            } catch (UnsupportedEncodingException e1) {
            } catch (FileNotFoundException e1) {
            } catch (IOException e) {
            }
        } else { // Date-view (håndterer både SingleDay og RawDB
            createDBFileIfNotExists();
            count_date_position_in_text=0;
            boolean date_position_in_text_found = false;
            date_marker = create_date_marker(yy, mm, dd);
            File file = new File(filePath, db_name_full);
            try {
                try (BufferedReader br = new BufferedReader 
                                        (new InputStreamReader(new FileInputStream(file), encoding))) {
                    boolean inside_date = false;
                    entry_txt = "";
                    String entry_txt_full;
                    entry_txt_full = "";
                    String thisLine;
                    while ((thisLine = br.readLine()) != null) {
                        entry_txt_full = entry_txt_full + thisLine + "\n";
                        if (!date_position_in_text_found) {
                            count_date_position_in_text = count_date_position_in_text + thisLine.length() + 1;
                        }
                        if (inside_date) {
                            if (thisLine.contains(date_flag)) { 
                                inside_date = false;
                            } else {
                                entry_txt = entry_txt + thisLine + "\n";
                            }
                        }
                        if(thisLine.contains(date_marker)) {
                            date_position_in_text_found = true;
                            marked_todo_in_file = thisLine.contains(todo_flag_sign);
                            inside_date = true;
                        }
                    }
                    if (!viewTypeSingleDay) entry_txt = entry_txt_full;
                }
            } catch (UnsupportedEncodingException e1) {
            } catch (FileNotFoundException e1) {
            } catch (IOException e) {
            }
        }
        prev_entrytext = entry_txt;
        prev_marked_done_by_button_entrytext = entry_txt;
        entrytext.setText(entry_txt);
        if (!viewTypeSingleDay && !jumbleView) {
            entrytext.positionCaret(count_date_position_in_text);
        }
    }

    private String create_date_marker(int yy, int mm, int dd) {
        // Lave en streng med dato-stemplet ud fra den valgte dato 
        // (som den findes i database-filerne)
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", 
            "Thursday", "Friday", "Saturday"};
        String parseDayName;
        Calendar parseDays = Calendar.getInstance();
        parseDays.set(Calendar.YEAR, yy);
        parseDays.set(Calendar.MONTH, mm);
        parseDays.set(Calendar.DAY_OF_MONTH, dd);
        parseDayName = dayNames[parseDays.get(Calendar.DAY_OF_WEEK)-1];
        String datemarkresult = date_flag + " " 
                + parseDayName + " " 
                + parseDays.get(Calendar.YEAR) + "-" 
                + String.format("%02d", (parseDays.get(Calendar.MONTH)+1)) + "-" 
                + String.format("%02d", parseDays.get(Calendar.DAY_OF_MONTH));
        return datemarkresult;
    }
    
    private void save_changed_entrytext() {
        // Gem den tekst, der ligger i entrytext til databasefilen
        String current_entrytext = entrytext.getText();
        if (jumbleView) { //Hvis vi er i jumbleview
            //Resultatet gemmes
            File jumblefile_write = new File(filePath, jumbleFileName);
            try {
                try (BufferedWriter bw = new BufferedWriter
                                (new OutputStreamWriter(new FileOutputStream(jumblefile_write),encoding))) {
                    bw.write(current_entrytext.replaceAll("\n", endline));
                }
            } catch (UnsupportedEncodingException e1) {
            } catch (FileNotFoundException e1) {
            } catch (IOException e) {
            }
        } else { //hvis vi er i date-view
            if (!current_entrytext.equals(prev_entrytext) || marked_done_by_button){
                //Hvis teksten er blevet ændret, skal ændringer gemmes
                File file_read = new File(filePath, db_name_full);
                File file_write = new File(filePath, "_"+db_name_full);
                if (!viewTypeSingleDay) { //RawDBView
                    try {
                        try (BufferedWriter bw = new BufferedWriter
                                        (new OutputStreamWriter(new FileOutputStream(file_write),encoding))) {
                            bw.write(current_entrytext.replaceAll("\n", endline));
                        }
                    } catch (UnsupportedEncodingException e1) {
                    } catch (FileNotFoundException e1) {
                    } catch (IOException e) {
                    }
                } else { //SingleDayView
                    if (!current_entrytext.endsWith("\n")) {
                            current_entrytext = current_entrytext + "\n\n";
                    }
                    try {
                        BufferedWriter bw;
                        try (BufferedReader br = new BufferedReader 
                                                (new InputStreamReader(new FileInputStream(file_read),encoding))) {
                            bw = new BufferedWriter
                                                (new OutputStreamWriter(new FileOutputStream(file_write),encoding));
                            boolean inside_date = false;
                            String thisLine;
                            while ((thisLine = br.readLine()) != null) {
                                if (inside_date) {
                                    if (thisLine.contains(date_flag)) {
                                        inside_date = false;
                                        bw.write(thisLine + endline);
                                    }
                                } else {
                                    if(thisLine.contains(date_marker)) {
                                        String writeline;
                                        String temp_marked_done;
                                        if ((marked_todo_in_file && !marked_done_by_button)
                                                || entrytext_changed_after_marked_done_by_button) {
                                            temp_marked_done = todo_flag;
                                        } else {
                                            temp_marked_done = "";
                                        }
                                        if (todo_flag.equals(todo_flag_sign)) {
                                            writeline = thisLine.replace(todo_flag,  "") + temp_marked_done + endline;
                                        } else {
                                            writeline = thisLine + endline;
                                        }
                                        
                                        bw.write(writeline);
                                        marked_todo_in_file = writeline.contains(todo_flag_sign);
                                        inside_date = true;
                                        //TODO: Det kunne nu være rart, hvis jeg kunne lave
                                        //det sådan, at der altid er mindst én ekstra
                                        //ekstra-linie mellem datoer...
                                        bw.write(current_entrytext.replaceAll("\n", endline));
                                    } else {
                                        bw.write(thisLine + endline);
                                    }
                                }
                            }
                            br.close();
                        }
                        bw.close();
                    } catch (UnsupportedEncodingException e1) {
                    } catch (FileNotFoundException e1) {
                    } catch (IOException e) {
                    }
                }
                file_read.delete();
                file_write.renameTo(file_read);
                marked_done_by_button = false;
            }
        }
        entrytext_changed = false;
        prev_entrytext = entrytext.getText();
    }

    private void change_year(int new_year) {
        currentyear = new_year;
        setDBNames(currentyear);
        undone_count = countUndone(currentyear);
        ButtonTexts();
        createDBFileIfNotExists();
    }

    private void createDBFileIfNotExists() {
        String name;
        int year_code;
        if (jumbleView) {
            name = jumbleFileName;
            year_code = 0;
        } else {
            name = db_name_full;
            year_code = currentyear;
        } 
        File currentDBFile = new File (filePath, name);
        if(!currentDBFile.exists()){
            //Create new file
            WriteDB(year_code, name, date_flag, endline, encoding, filePath);
        }
    }

    private void setPickedDayOfYear(int new_picked_day_of_year) {
        // Ændre datepicker til at være den valgte day-of-year
        Calendar selected_day = Calendar.getInstance();
        selected_day.set(Calendar.YEAR, currentyear);
        selected_day.set(Calendar.DAY_OF_YEAR, new_picked_day_of_year);
        
        LocalDate dt;
        dt = LocalDate.of(currentyear, selected_day.get(Calendar.MONTH)+1, 
                        selected_day.get(Calendar.DAY_OF_MONTH));
        date_picked.setValue(dt);
    }
}
