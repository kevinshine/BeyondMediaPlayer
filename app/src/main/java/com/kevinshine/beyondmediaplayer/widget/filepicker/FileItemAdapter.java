/*
 * Copyright (c) 2015 Jonas Kalderstam
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.kevinshine.beyondmediaplayer.widget.filepicker;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple adapter which also inserts a header item ".." to handle going up to the parent folder.
 * @param <T> the type which is used, for example a normal java File object.
 */
class FileItemAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final LogicHandler<T> mLogic;
    private SortedList<T> mList = null;
    // Start with first item selected
    private int focusedItem = 0;

    public FileItemAdapter(LogicHandler<T> logic) {
        this.mLogic = logic;
    }

    public void setList(SortedList<T> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // Handle key up and key down and attempt to move selection
        recyclerView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();

                // Return false if scrolled to the bounds and allow focus to move off the list
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        return tryMoveSelection(lm, 1);
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        return tryMoveSelection(lm, -1);
                    }
                }

                return false;
            }
        });
    }

    private boolean tryMoveSelection(RecyclerView.LayoutManager lm, int direction) {
        int tryFocusItem = focusedItem + direction;

        // If still within valid bounds, move the selection, notify to redraw, and scroll
        if (tryFocusItem >= 0 && tryFocusItem < getItemCount()) {
            lm.getChildAt(focusedItem).clearFocus();
            focusedItem = tryFocusItem;
            lm.getChildAt(tryFocusItem).requestFocus();
            lm.scrollToPosition(focusedItem);
            return true;
        }

        return false;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mLogic.onCreateViewHolder(parent, viewType);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int headerPosition) {
        if (headerPosition == 0) {
            mLogic.onBindHeaderViewHolder((AbstractFilePickerFragment<T>.HeaderViewHolder) viewHolder);
        } else {
            int pos = headerPosition - 1;
            mLogic.onBindViewHolder((AbstractFilePickerFragment<T>.DirViewHolder) viewHolder, pos, mList.get(pos));
        }
    }

    @Override
    public int getItemViewType(int headerPosition) {
        if (0 == headerPosition) {
            return LogicHandler.VIEWTYPE_HEADER;
        } else {
            int pos = headerPosition - 1;
            return mLogic.getItemViewType(pos, mList.get(pos));
        }
    }

    @Override
    public int getItemCount() {
        if (mList == null) {
            return 0;
        }

        // header + count
        return 1 + mList.size();
    }
}
