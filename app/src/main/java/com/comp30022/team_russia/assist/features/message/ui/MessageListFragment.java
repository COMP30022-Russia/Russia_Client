package com.comp30022.team_russia.assist.features.message.ui;

import static com.comp30022.team_russia.assist.features.profile.services.ProfileImageManager.lessResolution;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.comp30022.team_russia.assist.R;
import com.comp30022.team_russia.assist.base.BaseFragment;
import com.comp30022.team_russia.assist.base.TitleChangable;
import com.comp30022.team_russia.assist.base.di.Injectable;
import com.comp30022.team_russia.assist.databinding.FragmentMessageListBinding;
import com.comp30022.team_russia.assist.features.message.services.ChatService;
import com.comp30022.team_russia.assist.features.message.vm.MessageListViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;



/**
 * Chat History screen.
 */
public class MessageListFragment extends BaseFragment implements Injectable {

    private static final String TAG = "MessageListFragment";

    private MessageListViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    ChatService chatService;

    private FragmentMessageListBinding binding;

    private MessageListAdapter adapter;

    private ImageView cameraButton;

    private ImageView photoAlbumButton;

    public static final int TAKE_PHOTO_CODE = 0;

    public static final int PICK_IMAGE = 1;

    private static int MAX_IMAGE_SIZE = 400;

    private boolean userScrolling = false;

    private List<File> currentImageFilesToSend = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        getActivity().getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        viewModel = ViewModelProviders.of(this, viewModelFactory)
            .get(MessageListViewModel.class);

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_message_list, container, false);
        binding.setViewmodel(viewModel);
        binding.setLifecycleOwner(this);

        viewModel.title.observe(this, title -> {
            ((TitleChangable) getActivity()).updateTitle(title);
        });

        // using recycler view to display messages
        adapter = new MessageListAdapter(viewModel, chatService, this, getContext());
        configureRecyclerView();
        setupNavigationHandler(viewModel);
        subscribeToListChange();

        subscribeToPicturePlaceholder();

        // parse input arguments
        int associationId = getArguments().getInt("associationId");
        viewModel.setAssociationId(associationId);

        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = binding.reyclerViewMessageList;

        // hide keyboard when scrolling on recycler view
        recyclerView.setOnTouchListener((v, event) -> {
            InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);

            EditText editText = binding.editMessageField;
            imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
            return false;
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                try {
                    int lastCompletelyVisibleItemPosition =
                        ((LinearLayoutManager) recyclerView.getLayoutManager())
                            .findLastVisibleItemPosition();
                    viewModel.onScrolled(lastCompletelyVisibleItemPosition);
                } catch (Exception e) {
                    // do nothing
                }
            }

            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        synchronized (MessageListFragment.this) {
                            userScrolling =  false;
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        synchronized (MessageListFragment.this) {
                            userScrolling =  true;
                        }
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        synchronized (MessageListFragment.this) {
                            userScrolling =  true;
                        }
                        break;
                    default:
                        break;
                }

            }
        });

        recyclerView.addOnLayoutChangeListener(
            (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                Log.w(TAG, "layout changed, might need to scroll");

                synchronized (MessageListFragment.this) {
                    if (this.userScrolling) {
                        // do not scroll when the user is interacting
                        return;
                    }
                }
                Log.w(TAG, "scroll due to layout change");
                binding.reyclerViewMessageList.post(() -> {
                    binding.reyclerViewMessageList.smoothScrollToPosition(bottom);
                });
            });


        // camera button
        cameraButton = binding.cameraButton;
        cameraButton.setOnClickListener(v -> takePicture());

        // photo album button
        photoAlbumButton = binding.albumButton;
        photoAlbumButton.setOnClickListener(v -> chooseImage());
    }


    /***************************** SELECT PHOTO ALBUM. ********************************/

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


    /***************************** IMAGE FROM CAMERA/ALBUM. ********************************/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle photo album image
        if (requestCode == PICK_IMAGE && data != null) {

            try {
                FileDescriptor fd = this.getContext()
                    .getContentResolver()
                    .openFileDescriptor(data.getData(),"r")
                    .getFileDescriptor();

                Bitmap bitmap = lessResolution(fd, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);

                String filename = "image.jpg";
                //create a file to write bitmap data
                File file = new File(getContext().getCacheDir(), filename);
                file.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                List<File> files = new ArrayList<>();
                files.add(file);

                // send image to server
                viewModel.getPictureDtoFromServer(files.size());

                if (! currentImageFilesToSend.isEmpty()) {
                    currentImageFilesToSend.clear();
                }
                for (File f : files) {
                    currentImageFilesToSend.add(f);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // handle camera image
        if (requestCode == TAKE_PHOTO_CODE && data != null) {
            try {

                Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                String filename = "image.jpg";
                //create a file to write bitmap data
                File file = new File(getContext().getCacheDir(), filename);
                file.createNewFile();

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapdata = bos.toByteArray();

                //write the bytes in file
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapdata);
                fos.flush();
                fos.close();

                List<File> files = new ArrayList<>();
                files.add(file);

                // send image to server
                viewModel.getPictureDtoFromServer(files.size());

                if (! currentImageFilesToSend.isEmpty()) {
                    currentImageFilesToSend.clear();
                }
                for (File f : files) {
                    currentImageFilesToSend.add(f);
                }

                // todo send image into chat bubble for the SENDER

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /***************************** TAKE PHOTO. ********************************/


    private void takePicture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, TAKE_PHOTO_CODE);
    }


    /***************************** LOAD MESSAGE. ********************************/

    private void configureRecyclerView() {
        RecyclerView recyclerView = binding.reyclerViewMessageList;
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void subscribeToListChange() {
        viewModel.messageList.observe(this, newMessageList -> {
            if (newMessageList != null) {
                adapter.setMessageList(newMessageList);
            }
            binding.executePendingBindings();
            Log.w(TAG, "scroll to bottom due to new items");
            binding.reyclerViewMessageList.scrollToPosition(adapter.getItemCount() - 1);
        });
    }


    private void subscribeToPicturePlaceholder() {
        viewModel.pictureDtoList.observe(this, newPictureDtoList -> {
            if (newPictureDtoList != null) {

                for (int i = 0; i < newPictureDtoList.size(); i++) {

                    File file = currentImageFilesToSend.get(i);

                    int pictureId = newPictureDtoList.get(i).getPictureId();

                    viewModel.sendImageToServer(pictureId, file);
                }
            }
        });
    }


    /**************************** Detail Button. *********************************/

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_messaging, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.detail_icon:
            viewModel.onDetailButtonClicked();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
}