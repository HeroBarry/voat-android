package co.voat.android.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import co.voat.android.R;
import co.voat.android.VoatApp;
import co.voat.android.api.CommentsResponse;
import co.voat.android.api.VoatClient;
import co.voat.android.data.Comment;
import co.voat.android.data.Submission;
import co.voat.android.dialogs.CommentDialog;
import co.voat.android.events.ShowContextualMenuEvent;
import co.voat.android.viewHolders.CommentViewHolder;
import co.voat.android.viewHolders.CommentsHeaderViewHolder;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Comments on comments on comments
 * Created by Jawn on 6/13/2015.
 */
public class CommentFragment extends BaseFragment {
    private static final String EXTRA_SUBMISSION = "extra_submission";

    public static CommentFragment newInstance(Submission submission) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_SUBMISSION, submission);
        fragment.setArguments(args);
        return fragment;
    }

    @InjectView(R.id.comment_list)
    RecyclerView commentList;

    @OnClick(R.id.fab)
    void onFabClick(View v) {
        new CommentDialog(getActivity(), submission).show();
    }

    Submission submission;
    ViewGroup commentMenu;

    private final View.OnClickListener commentReplyListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "reply", Toast.LENGTH_SHORT)
                    .show();
            commentMenu.setVisibility(View.GONE);
        }
    };

    private final View.OnClickListener upVoteCommentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "upVote", Toast.LENGTH_SHORT)
                    .show();
            commentMenu.setVisibility(View.GONE);
        }
    };

    private final View.OnClickListener downVoteCommentListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "downvote", Toast.LENGTH_SHORT)
                    .show();
            commentMenu.setVisibility(View.GONE);
        }
    };

    private final View.OnClickListener profileListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Toast.makeText(getActivity(), "Profile", Toast.LENGTH_SHORT)
                    .show();
            commentMenu.setVisibility(View.GONE);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        //commentMenu = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.action_comments, null);
        commentMenu = (ViewGroup) view.findViewById(R.id.action_root);
        commentMenu.findViewById(R.id.action_reply).setOnClickListener(commentReplyListener);
        commentMenu.findViewById(R.id.action_upvote).setOnClickListener(upVoteCommentListener);
        commentMenu.findViewById(R.id.action_downvote).setOnClickListener(downVoteCommentListener);
        commentMenu.findViewById(R.id.action_profile).setOnClickListener(profileListener);

        submission = (Submission) getArguments().getSerializable(EXTRA_SUBMISSION);
        commentList.setLayoutManager(new LinearLayoutManager(getActivity()));
        VoatClient.instance().getComments(submission.getSubverse(), submission.getId(), new Callback<CommentsResponse>() {
            @Override
            public void success(CommentsResponse commentsResponse, Response response) {
                if (commentsResponse.success) {
                    commentList.setAdapter(new CommentAdapter(submission, commentsResponse.data));
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.e(error.toString());
                Snackbar.make(getActivity().getWindow().getDecorView(), getString(R.string.error), Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
    }

    public class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_COMMENT = 1;

        private Submission submission;
        private List<Comment> comments;

        public Comment getValueAt(int position) {
            return comments.get(position-1);
        }

        public CommentAdapter(Submission submission, List<Comment> items) {
            comments = items;
            this.submission = submission;
        }

        private final View.OnLongClickListener onCommentLongClick = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                VoatApp.bus().post(new ShowContextualMenuEvent());
                return true;
            }
        };

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                return CommentsHeaderViewHolder.create(parent);
            } else if (viewType == TYPE_COMMENT) {
                RecyclerView.ViewHolder holder = CommentViewHolder.create(parent);
                holder.itemView.setOnLongClickListener(onCommentLongClick);
                ((CommentViewHolder)holder).contentText.setOnLongClickListener(onCommentLongClick);
                return holder;
            }
            throw new IllegalArgumentException("No view type matches");
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CommentViewHolder) {
                Comment comment = getValueAt(position);
                ((CommentViewHolder) holder).bind(comment);
                holder.itemView.setTag(R.id.list_position, position);
            } else if (holder instanceof CommentsHeaderViewHolder) {
                ((CommentsHeaderViewHolder) holder).bind(submission);
            }
        }

        @Override
        public int getItemCount() {
            return comments.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position)) {
                return TYPE_HEADER;
            } else {
                return TYPE_COMMENT;
            }
        }

        private boolean isPositionHeader(int position) {
            return position == 0;
        }
    }
}
