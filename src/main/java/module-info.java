module com.smartstudent {
    requires javafx.controls;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    exports com.smartstudent;
    exports com.smartstudent.model;
    exports com.smartstudent.dsa;
    exports com.smartstudent.service;
    exports com.smartstudent.storage;
    exports com.smartstudent.ui;

    opens com.smartstudent.model to com.fasterxml.jackson.databind, javafx.base;
}
