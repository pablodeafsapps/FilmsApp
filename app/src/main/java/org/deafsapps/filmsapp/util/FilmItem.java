package org.deafsapps.filmsapp.util;

public class FilmItem
{
    private String filmTitle;
    private String filmYear;   // Should be an integer, but some films have 'null' value
    private Ids filmIds;
    private String filmOverview;
    private String filmTrailer;
    private float filmRating;
    private long filmVotes;
    private Imgs filmImages;

    public FilmItem(String aTitle, String aYear, int anIdTrakt, String anIdSlug, String anIdImdb, int anIdTmdb, String anOverview, String aTrailer, float aRating, long someVotes, String aPoster, String aLogo)
    {
        this.filmTitle = aTitle;
        this.filmYear = aYear;
        this.filmIds = new Ids(anIdTrakt, anIdSlug, anIdImdb, anIdTmdb);
        this.filmOverview = anOverview;
        this.filmTrailer = aTrailer;
        this.filmRating = aRating;
        this.filmImages = new Imgs(aPoster, aLogo);
    }

    public String getFilmTitle() { return this.filmTitle; }

    public String getFilmYear() { return this.filmYear; }

    public Ids getFilmIds() { return this.filmIds; }

    public String getFilmOverview() { return this.filmOverview; }

    public String getFilmTrailer() { return this.filmTrailer; }

    public float getFilmRating() { return this.filmRating; }

    public long getFilmVotes() { return this.filmVotes; }

    public Imgs getFilmImages() { return this.filmImages; }

    public class Ids
    {

        private int filmIdTrakt;
        private String filmIdSlug;
        private String filmIdImdb;
        private int filmIdTmdb;

        private Ids(int aTrakt, String aSlug, String anImdb, int aTmdb)
        {
            this.filmIdTrakt = aTrakt;
            this.filmIdSlug = aSlug;
            this.filmIdImdb = anImdb;
            this.filmIdTmdb = aTmdb;
        }

        public int getFilmIdTrakt() { return filmIdTrakt; }

        public String getFilmIdSlug() { return filmIdSlug; }

        public String getFilmIdImdb() { return filmIdImdb; }

        public int getFilmIdTmdb() { return filmIdTmdb; }
    }

    public class Imgs
    {
        private String filmPoster;
        private String filmLogo;

        private Imgs(String aPoster, String aLogo)
        {
            this.filmPoster = aPoster;
            this.filmLogo = aLogo;
        }

        public String getFilmPoster() { return filmPoster; }

        public String getFilmLogo() { return filmLogo; }
    }
}
