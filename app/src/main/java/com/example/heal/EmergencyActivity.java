package com.example.heal;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class EmergencyActivity extends AppCompatActivity {

    private RecyclerView rvHelplines;
    private HelplineAdapter adapter;
    private List<Helpline> helplineList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        rvHelplines = findViewById(R.id.rvHelplines);
        rvHelplines.setLayoutManager(new LinearLayoutManager(this));

        initHelplines();
        
        adapter = new HelplineAdapter(this, helplineList);
        rvHelplines.setAdapter(adapter);
    }

    private void initHelplines() {
        helplineList = new ArrayList<>();
        
        // National Medical Helplines
        helplineList.add(new Helpline("1123 - Tele-Tabeeb", "1123", "Free 24/7 medical consultation with doctors and psychiatrists."));
        helplineList.add(new Helpline("1166 - Sehat Tahaffuz", "1166", "Health helpline for medical information and vaccination queries."));
        
        // Emergency & Ambulance Services
        helplineList.add(new Helpline("115 - Edhi Ambulance", "115", "National emergency medical response and ambulance service."));
        helplineList.add(new Helpline("1122 - Rescue & Disaster", "1122", "Emergency rescue and disaster response services."));
        helplineList.add(new Helpline("1020 - Chhipa Ambulance", "1020", "24/7 emergency ambulance service."));
        
        // IHS Home Medical Services
        helplineList.add(new Helpline("111-DOCTOR (IHS)", "111362867", "Private home healthcare service available in major cities."));
        
        // Polio / Vaccination
        helplineList.add(new Helpline("Polio Helpline (WhatsApp)", "03467776546", "Information and support for polio vaccination."));

    }
}
