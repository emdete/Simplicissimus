package de.emdete.android.wizard;

import de.emdete.android.gui.R;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import org.codepond.wizardroid.layouts.BasicWizardLayout;
import org.codepond.wizardroid.persistence.ContextVariable;
import org.codepond.wizardroid.WizardFlow;
import org.codepond.wizardroid.WizardStep;

public class TutorialActivity extends FragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
    }
}
