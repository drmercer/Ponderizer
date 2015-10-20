package net.danmercer.ponderizer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddScriptureReferenceActivity extends AppCompatActivity {

    private EditText editText;
    private TextView outText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_scripture_reference);

        outText = (TextView) findViewById(R.id.outTextView);

        editText = (EditText) findViewById(R.id.scriptureEntry);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    parseScriptureReference();
                    return true;
                }
                return false;
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null) {
                    parseScriptureReference();
                    return true;
                }
                return false;
            }
        });
        Button b = (Button) findViewById((R.id.button));
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                parseScriptureReference();
            }
        });
    }

    private void parseScriptureReference() {
        // Remove focus from EditText
        editText.clearFocus();

        // Get text from EditText
        String ref = editText.getText().toString().trim().toLowerCase();
        String refNoSpace = ref.replaceAll(" ", "");

        // Compare with Regex
        Pattern pattern = Pattern.compile("^(\\d?[A-z]+)(\\d+):((?:\\d+(?:,|-))+\\d+)");
        Matcher m = pattern.matcher(refNoSpace);

        // Output for debugging
        StringBuilder sb = new StringBuilder();
        sb.append(ref).append("\n");
        sb.append(refNoSpace).append("\n");

        boolean matches = m.matches();
        sb.append(matches ? "match\n" : "no match\n");

        try {
            sb.append("# of groups:");
            int groupCount = m.groupCount();
            sb.append(groupCount).append("\n");

            sb.append("book: ").append(m.group(1)).append('\n');
            sb.append("chapter: ").append(m.group(2)).append('\n');

            sb.append("verses: ");
            String versesString = m.group(3);
            sb.append(versesString).append('\n');

            String[] versesRanges = versesString.split(",");
            SequentialNumbersList verses = new SequentialNumbersList();
            for (String s : versesRanges) {
                if (s.startsWith("-") || s.endsWith("-")) {
                    throw new NumberFormatException("Verses entered were not formatted properly.");
                }
                try {
                    verses.add(Integer.valueOf(s));
                    Log.d("Numbers", "added single verse " + s);
                } catch (NumberFormatException e) {
                    Log.d("Numbers", "analyzing range    " + s);
                    String[] range = s.split("-");
                    int first = Integer.valueOf(range[0]);
                    int last = Integer.valueOf(range[range.length - 1]);
                    for (int i = first; i <= last; i++) {
                        verses.add(i);
                        Log.d("Numbers", "added single verse " + i);
                    }
                }
            }

            sb.append("expanded: ");
            for (Integer i : verses) {
                sb.append(i).append(", ");
            }
            outText.setText(sb.toString());

        } catch (Exception e) {
            Toast.makeText(this, "Please enter a valid scripture reference.", Toast.LENGTH_LONG).show();
        }

        // TODO: download those verses from the internet, and put them into the app
    }

    private class SequentialNumbersList extends ArrayList<Integer> {
        private int lastAdded = Integer.MIN_VALUE;

        @Override
        public boolean add(Integer object) {
            if (object < lastAdded) {
                throw new IllegalArgumentException();
            } else if (object == lastAdded) {
                return false;
            }
            return super.add(object);
        }
    }
}
