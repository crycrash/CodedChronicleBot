package org.example;

import java.util.ArrayList;
import java.util.List;

public class Calendar {
    List<ArrayList<Integer>> calendar = new ArrayList<>();
    public Calendar(){
        for (int m = 1; m <= 12; m++) {
            ArrayList<Integer> daysInMonth = new ArrayList<>();
            int daysCount;
            if ((m == 1) || (m == 3) || (m == 5) || (m == 7) || (m == 8) || (m == 10) || (m == 12))
                daysCount = 31;
            else if ((m == 4) || (m == 6) || (m == 9) || (m == 11))
                daysCount = 30;
            else
                daysCount = 29;
            for (int day = 1; day <= daysCount; day++) {
                daysInMonth.add(day);
            }
            calendar.add(daysInMonth);
        }
    }
}
