package net.dean.jraw.paginators;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.MultiReddit;
import net.dean.jraw.models.Submission;

/**
 * Iterates through the posts in a multireddit.
 */
public class MultiRedditPaginator extends Paginator<Submission> {

    private MultiReddit multiReddit;
    private String path;

    /**
     * Instantiates a new MultiRedditPaginator
     *
     * @param creator The RedditClient that will be used to send HTTP requests
     */
    public MultiRedditPaginator(RedditClient creator) {
        super(creator, Submission.class);
    }

    /**
     * Instantiates a new MultiRedditPaginator
     *
     * @param creator The RedditClient that will be used to send HTTP requests
     * @param multi The multireddit to iterate
     */
    public MultiRedditPaginator(RedditClient creator, MultiReddit multi) {
        super(creator, Submission.class);
        this.multiReddit = multi;
        this.path = multi.getPath();
    }

    /**
     * Instantiates a new MultiRedditPaginator
     *
     * @param creator The RedditClient that will be used to send HTTP requests
     * @param path The multireddit path to iterate
     */
    public MultiRedditPaginator(RedditClient creator, String path) {
        super(creator, Submission.class);
        this.path = path;
    }

    @Override
    protected String getBaseUri() {
        String path = sorting == null ? "" : "/" + sorting.name().toLowerCase();
        return this.path + path;
    }

    /**
     * Gets the MultiReddit that this paginator is iterating through
     * @return The MultiReddit
     */
    public MultiReddit getMultiReddit() {
        return multiReddit;
    }

    /**
     * Sets the MultiReddit to iterate through
     * @param multiReddit The new MultiReddit
     */
    public void setMultiReddit(MultiReddit multiReddit) {
        this.multiReddit = multiReddit;
        this.path = multiReddit.getPath();
        invalidate();
    }

    /**
     * Sets the MultiReddit path to iterate through
     * @param path The new MultiReddit path
     */
    public void setPath(String path) {
        this.path = path;
        invalidate();
    }
}
