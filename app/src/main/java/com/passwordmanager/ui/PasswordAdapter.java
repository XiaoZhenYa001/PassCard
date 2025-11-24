package com.passwordmanager.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.card.MaterialCardView;
import com.passwordmanager.utils.TransparencyManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.passwordmanager.R;
import com.passwordmanager.model.Password;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {
    private List<Password> passwords = new ArrayList<>();

    // DiffUtil Callback for efficient RecyclerView updates
    private static class PasswordDiffCallback extends DiffUtil.Callback {
        private final List<Password> oldList;
        private final List<Password> newList;

        public PasswordDiffCallback(List<Password> oldList, List<Password> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
        }
    }
    private boolean isMultiSelectMode = false;
     private List<Password> selectedPasswords = new ArrayList<>();
    private OnMultiSelectModeChangeListener multiSelectModeChangeListener;

    public List<Password> getPasswords() {
        return passwords;
    }

    public List<Password> getSelectedPasswords() {
        return selectedPasswords;
    }

    public interface OnMultiSelectModeChangeListener {
        void onMultiSelectModeChanged(boolean isMultiSelectMode, int selectedCount);
    }

    public void setOnMultiSelectModeChangeListener(OnMultiSelectModeChangeListener listener) {
        this.multiSelectModeChangeListener = listener;
    }
//进入多选模式
    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_password, parent, false);
        PasswordViewHolder holder = new PasswordViewHolder(view);
        
        // 添加长按监听器
        //进入多选模式
        /*
        holder.itemView.setOnLongClickListener(v -> {
            if (!isMultiSelectMode) {
                isMultiSelectMode = true;
                // 自动选中当前长按的密码便签
                selectedPasswords.add(passwords.get(holder.getBindingAdapterPosition()));
                // 添加震动反馈
                android.os.VibratorManager vibratorManager = (android.os.VibratorManager) v.getContext().getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (vibratorManager != null) {
                    android.os.Vibrator vibrator = vibratorManager.getDefaultVibrator();
                    android.os.VibrationEffect vibrationEffect = android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE);
                    vibrator.vibrate(vibrationEffect);
                }
                if (multiSelectModeChangeListener != null) {
                    multiSelectModeChangeListener.onMultiSelectModeChanged(true, selectedPasswords.size());
                }
                notifyDataSetChanged();
            }
            return true;
        });
        */
        
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        Password password = passwords.get(position);
        
        // 应用密码卡片透明度
        View itemView = holder.itemView;
        if (itemView instanceof MaterialCardView) {
            MaterialCardView cardView = (MaterialCardView) itemView;
            if (isMultiSelectMode && selectedPasswords.contains(password)) {
                // 选中状态时设置为完全不透明
                cardView.setAlpha(1.0f);
            } else {
                // 未选中状态时使用用户设置的透明度
                TransparencyManager.applyPasswordCardAlpha(itemView.getContext(), cardView);
            }
        }

        // 处理多选模式下的选择状态
        if (isMultiSelectMode) {
            holder.checkButton.setVisibility(View.VISIBLE);
            holder.buttonMore.setVisibility(View.GONE);
            holder.buttonCopyPassword.setVisibility(View.GONE);
            holder.buttonViewDetails.setVisibility(View.GONE);
            boolean isSelected = selectedPasswords.contains(password);
            holder.checkButton.setImageResource(isSelected ? R.drawable.ic_check_circle_filled : R.drawable.ic_nocheck_circle);
            
            // 为选择按钮添加点击事件
            holder.checkButton.setOnClickListener(v -> {
                if (isSelected) {
                    selectedPasswords.remove(password);
                } else {
                    selectedPasswords.add(password);
                }
                if (multiSelectModeChangeListener != null) {
                    multiSelectModeChangeListener.onMultiSelectModeChanged(true, selectedPasswords.size());
                }
                // 立即更新当前项的透明度
                if (itemView instanceof MaterialCardView) {
                    MaterialCardView cardView = (MaterialCardView) itemView;
                    if (selectedPasswords.contains(password)) {
                        cardView.setAlpha(1.0f);
                    } else {
                        TransparencyManager.applyPasswordCardAlpha(itemView.getContext(), cardView);
                    }
                }
                notifyItemChanged(position);
            });
            
            // 将点击事件扩展到整个itemView
            holder.itemView.setOnClickListener(v -> {
                if (isSelected) {
                    selectedPasswords.remove(password);
                } else {
                    selectedPasswords.add(password);
                }
                if (multiSelectModeChangeListener != null) {
                    multiSelectModeChangeListener.onMultiSelectModeChanged(true, selectedPasswords.size());
                }
                // 立即更新当前项的透明度
                if (itemView instanceof MaterialCardView) {
                    MaterialCardView cardView = (MaterialCardView) itemView;
                    if (selectedPasswords.contains(password)) {
                        cardView.setAlpha(1.0f);
                    } else {
                        TransparencyManager.applyPasswordCardAlpha(itemView.getContext(), cardView);
                    }
                }
                notifyItemChanged(position);
            });
        } else {
            holder.checkButton.setVisibility(View.GONE);
            holder.buttonMore.setVisibility(View.VISIBLE);
            holder.buttonCopyPassword.setVisibility(View.VISIBLE);
            holder.buttonViewDetails.setVisibility(View.VISIBLE);
            holder.itemView.setOnClickListener(null); // 清除多选模式的点击事件
        }
        
        // 服务名称始终显示，这是必须的字段
        holder.serviceTextView.setText("服务: " + password.getService());
        
        // 初始化所有字段为不可见
        holder.usernameTextView.setVisibility(View.GONE);
        holder.phoneTextView.setVisibility(View.GONE);
        holder.emailTextView.setVisibility(View.GONE);
        
        // 显示所有非空字段
        if (!isEmpty(password.getUsername())) {
            holder.usernameTextView.setText("用户名: " + password.getUsername());
            holder.usernameTextView.setVisibility(View.VISIBLE);
        }
        
        if (!isEmpty(password.getPhoneNumber())) {
            holder.phoneTextView.setText("手机号: " + password.getPhoneNumber());
            holder.phoneTextView.setVisibility(View.VISIBLE);
        }
        
        if (!isEmpty(password.getEmail())) {
            holder.emailTextView.setText("邮箱: " + password.getEmail());
            holder.emailTextView.setVisibility(View.VISIBLE);
        }

        // 设置复制密码按钮点击事件
        holder.buttonCopyPassword.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("密码", password.getPassword());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(v.getContext(), "密码已复制到剪贴板", Toast.LENGTH_SHORT).show();
        });

        // 设置查看详情按钮点击事件
        holder.buttonViewDetails.setOnClickListener(v -> {
            Context context = v.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            StringBuilder allInfo = new StringBuilder();
            
            // 只添加非空字段
            if (!isEmpty(password.getService())) {
                allInfo.append("服务：").append(password.getService()).append("\n");
            }
            if (!isEmpty(password.getUsername())) {
                allInfo.append("用户名：").append(password.getUsername()).append("\n");
            }
            if (!isEmpty(password.getPhoneNumber())) {
                allInfo.append("手机号：").append(password.getPhoneNumber()).append("\n");
            }
            if (!isEmpty(password.getEmail())) {
                allInfo.append("邮箱：").append(password.getEmail()).append("\n");
            }
            if (!isEmpty(password.getPassword())) {
                allInfo.append("密码：").append(password.getPassword()).append("\n");
            }
            if (!isEmpty(password.getNote())) {
                allInfo.append("备注：").append(password.getNote());
            }
            
            builder.setTitle("密码详情")
                    .setMessage(allInfo.toString())
                    .setPositiveButton("复制全部", (dialog, which) -> {
                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("密码信息", allInfo.toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "已复制所有信息到剪贴板", Toast.LENGTH_SHORT).show();
                    })
                    .show();
        });

        // 设置更多按钮点击事件
        holder.buttonMore.setOnClickListener(v -> {
            Context context = v.getContext();
            PopupMenu popup = new PopupMenu(context, holder.buttonMore);
            popup.getMenuInflater().inflate(R.menu.password_item_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_edit) {
                    showEditDialog(context, password);
                    return true;
                } else if (itemId == R.id.menu_delete) {
                    showDeleteConfirmDialog(context, password);
                    return true;
                }
                return false;
            });

            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public void setPasswords(List<Password> newPasswords) {
        if (this.passwords == null) {
            this.passwords = newPasswords;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new PasswordDiffCallback(this.passwords, newPasswords));
            this.passwords.clear();
            this.passwords.addAll(newPasswords);
            diffResult.dispatchUpdatesTo(this);
        }
    }

    public void toggleMultiSelectMode(boolean enable) {
        isMultiSelectMode = enable;
        if (!enable) {
            selectedPasswords.clear();
        }
        notifyDataSetChanged();
        if (multiSelectModeChangeListener != null) {
            multiSelectModeChangeListener.onMultiSelectModeChanged(enable, enable ? selectedPasswords.size() : 0);
        }
    }

    public void selectAll() {
        selectedPasswords.clear();
        selectedPasswords.addAll(passwords);
        notifyDataSetChanged();
    }

    static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView serviceTextView;
        TextView usernameTextView;
        TextView phoneTextView;
        TextView emailTextView;
        ImageButton buttonMore;
        View buttonCopyPassword;
        View buttonViewDetails;
        ImageButton checkButton;

        PasswordViewHolder(View itemView) {
            super(itemView);
            serviceTextView = itemView.findViewById(R.id.text_service);
            usernameTextView = itemView.findViewById(R.id.text_username);
            phoneTextView = itemView.findViewById(R.id.text_phone);
            emailTextView = itemView.findViewById(R.id.text_email);
            buttonMore = itemView.findViewById(R.id.button_more);
            buttonCopyPassword = itemView.findViewById(R.id.button_copy_password);
            buttonViewDetails = itemView.findViewById(R.id.button_view_details);
            checkButton = itemView.findViewById(R.id.button_check);
        }
    }

    private void showEditDialog(Context context, Password password) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("编辑密码");
        
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_password, null);
        builder.setView(dialogView);
        
        TextInputEditText serviceEdit = dialogView.findViewById(R.id.edit_service);
        TextInputEditText usernameEdit = dialogView.findViewById(R.id.edit_username);
        TextInputEditText phoneEdit = dialogView.findViewById(R.id.edit_phone);
        TextInputEditText emailEdit = dialogView.findViewById(R.id.edit_email);
        TextInputEditText passwordEdit = dialogView.findViewById(R.id.edit_password);
        TextInputEditText noteEdit = dialogView.findViewById(R.id.edit_note);
        
        // 预填充现有数据
        serviceEdit.setText(password.getService());
        usernameEdit.setText(password.getUsername());
        phoneEdit.setText(password.getPhoneNumber());
        emailEdit.setText(password.getEmail());
        passwordEdit.setText(password.getPassword());
        noteEdit.setText(password.getNote());
        
        // 创建保存操作的Runnable
        Runnable saveAction = () -> {
            String service = serviceEdit.getText().toString().trim();
            String username = usernameEdit.getText().toString().trim();
            String phone = phoneEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String newPassword = passwordEdit.getText().toString().trim();
            String note = noteEdit.getText().toString().trim();
            
            if (service.isEmpty() && username.isEmpty() && phone.isEmpty() && email.isEmpty() && newPassword.isEmpty()) {
                Toast.makeText(context, "请至少填写一个字段", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Password updatedEntry = new Password(service, username, phone, email, newPassword, note);
            updatedEntry.setId(password.getId()); // 保持ID不变
            
            // 通过MainActivity更新数据
            if (context instanceof MainActivity) {
                ((MainActivity) context).getPasswordViewModel().update(updatedEntry);
            }
        };
        
        builder.setPositiveButton("保存", (dialog, which) -> saveAction.run());
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        
        // 设置点击对话框外部区域的行为
        builder.setCancelable(true);
        builder.setOnCancelListener(dialog -> saveAction.run());
        
        builder.show();
    }

    private void showDeleteConfirmDialog(Context context, Password password) {
        new AlertDialog.Builder(context)
                .setTitle("删除确认")
                .setMessage("确定要删除这条密码记录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).getPasswordViewModel().delete(password);
                        Toast.makeText(context, "密码已删除", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    // 获取第一个非空字段
    private String getFirstNonEmptyField(Password password) {
        // 按优先级顺序检查字段
        if (!isEmpty(password.getUsername())) {
            return "用户名: " + password.getUsername();
        }
        if (!isEmpty(password.getPhoneNumber())) {
            return "手机号: " + password.getPhoneNumber();
        }
        if (!isEmpty(password.getEmail())) {
            return "邮箱: " + password.getEmail();
        }
        if (!isEmpty(password.getService())) {
            return "服务: " + password.getService();
        }
        if (!isEmpty(password.getNote())) {
            String note = password.getNote();
            if (note.length() > 50) {
                note = note.substring(0, 47) + "...";
            }
            return "备注: " + note;
        }
        return null;
    }

    // 检查字符串是否为空
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}