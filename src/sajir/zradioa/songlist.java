package sajir.zradioa;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import sajir.zradio.R;

public class songlist extends ListActivity implements OnItemClickListener{
	

	Bundle extras;
	String[] lista;

	public void onCreate(Bundle savedInstanceState) {
		  super.onCreate(savedInstanceState);
		  
		  extras = getIntent().getExtras();
		  lista = extras.getStringArray("lista");
		  
		  setListAdapter(new ArrayAdapter<String>(this, R.layout.songlist, lista));

		  ListView lv = getListView();
		  lv.setTextFilterEnabled(true);

		  lv.setOnItemClickListener(this);
		}

	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		
		String url = "https://www.google.co.ve/search?q="+((TextView) view).getText();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);

		   

		
	}
	
}
