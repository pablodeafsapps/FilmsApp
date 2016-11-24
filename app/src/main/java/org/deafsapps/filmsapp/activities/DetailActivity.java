package org.deafsapps.filmsapp.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.deafsapps.filmsapp.R;
import org.deafsapps.filmsapp.util.PatchedTextView;

public class DetailActivity extends AppCompatActivity
{
    private static final String TAG_DETAIL_ACTIVITY = "In-DetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //----- TOOLBAR -----
        // Getting a reference to the 'Toolbar' and adding it as ActionBar for the 'Activity'
        final Toolbar mToolbar = (Toolbar) this.findViewById(R.id.detailActToolbar);
        // This coming line makes the magic, replacing the 'ActionBar' with the 'Toolbar'
        this.setSupportActionBar(mToolbar);

        final ActionBar mActionBar = this.getSupportActionBar();
        if (mActionBar != null)
        {
            // This line actually shows a 'Button' on the top left corner of the 'ActionBar'/'Toolbar',
            // whose behaviour can be defined through the 'onOptionsItemSelected' method
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        //--------------------------------------

        // The following line allows to set up the text fields from the current piece of news
        this.setUpTextFields();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem whichItem)
    {
        if (whichItem.getItemId() == android.R.id.home)
            this.onBackPressed();
        else
            return false;

        return true;
    }

    private void setUpTextFields()
    {
        final TextView detailActivTitle = (TextView) this.findViewById(R.id.detailTitleTxtView);
            detailActivTitle.setText(this.getIntent().getStringExtra("title"));
        final TextView detailActivYear = (TextView) this.findViewById(R.id.detailYearTxtView);
            detailActivYear.setText("Released in " + this.getIntent().getStringExtra("year"));
        Picasso.with(this).load(this.getIntent().getStringExtra("logo")).into((ImageView) this.findViewById(R.id.detailActivLogo));
        final TextView detailActivId = (TextView) this.findViewById(R.id.detailIdTxtView);
            detailActivId.setText("Imdb: " + this.getIntent().getStringExtra("imdb"));
        final TextView detailActivRating = (TextView) this.findViewById(R.id.detailRatingTxtView);
            detailActivRating.setText("Rating: " + String.valueOf(this.getIntent().getFloatExtra("rating", 0)));
        final PatchedTextView detailActivBody = (PatchedTextView) this.findViewById(R.id.detailBodyTxtView);
            // The next line allows to make clickable any link in the text
            detailActivBody.setMovementMethod(LinkMovementMethod.getInstance());
            // This next line sets the 'TextView' object text excluding any image tag
            detailActivBody.setText(Html.fromHtml(this.getIntent().getStringExtra("overview")));
        //Picasso.with(this).load(this.getIntent().getStringExtra("poster")).into((ImageView) this.findViewById(R.id.detailActivPoster));
    }

    @Override
    public void onBackPressed()
    {
        this.finish();

        // In this case, 'super.onBackPressed()' needs to be the last line of the method
        super.onBackPressed();
    }
}
