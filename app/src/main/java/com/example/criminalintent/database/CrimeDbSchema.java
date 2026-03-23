package com.example.criminalintent.database;

public class CrimeDbSchema {
    public static final class CrimeTable {
        public static final String NAME = "crimes";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
            public static final String SUSPECT_NUMBER = "suspect_number";
            public static final String POLICE_NAME = "police_name";
            public static final String POLICE_NUMBER = "police_number";
        }
    }
}
