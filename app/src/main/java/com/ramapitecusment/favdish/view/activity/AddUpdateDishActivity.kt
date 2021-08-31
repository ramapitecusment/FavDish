package com.ramapitecusment.favdish.view.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.ramapitecusment.favdish.R
import com.ramapitecusment.favdish.application.FavDishApplication
import com.ramapitecusment.favdish.view.adapters.DialogListItemAdapter
import com.ramapitecusment.favdish.databinding.ActivityAddUpdateDishBinding
import com.ramapitecusment.favdish.databinding.DialogCustomImageSelectionBinding
import com.ramapitecusment.favdish.databinding.DialogListBinding
import com.ramapitecusment.favdish.repository.database.FavDish
import com.ramapitecusment.favdish.utils.Constants
import com.ramapitecusment.favdish.view.adapters.OnCategoryClickListener
import com.ramapitecusment.favdish.viewmodel.FavDishViewModel
import com.ramapitecusment.favdish.viewmodel.FavDishViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_CAMERA = 1
        private const val REQUEST_CODE_GALLERY = 2
        private const val IMAGE_DIRECTORY = "FavDishDirectory"
        private const val TAG = "DataToUnderstand"
    }

    private lateinit var binding: ActivityAddUpdateDishBinding
    private var imagePath: String = ""
    private lateinit var customListDialog: Dialog
    private var updateDish: FavDish? = null
    private var isUpdate: Boolean? = null

    private val viewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isUpdate = checkSetupIntent()
        setupFields()
        setupActionBar()
        setOnClickListeners()
    }

    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setOnClickListeners() {
        binding.ivAddImageButton.setOnClickListener {
            customImageSelectionDialog()
        }

        binding.btnAddDish.setOnClickListener {
            insertOrUpdateDish()
        }

        binding.edType.setOnClickListener {
            showDialogListCategory(
                getString(R.string.title_select_dish_type),
                Constants.dishTypes(),
                Constants.DISH_TYPE
            )
        }

        binding.edCategory.setOnClickListener {
            showDialogListCategory(
                getString(R.string.title_select_dish_category),
                Constants.dishCategories(),
                Constants.DISH_CATEGORY
            )
        }

        binding.edCookingTime.setOnClickListener {
            showDialogListCategory(
                getString(R.string.title_select_dish_cooking_time),
                Constants.dishCookTime(),
                Constants.DISH_COOKING_TIME
            )
        }
    }

    private fun checkSetupIntent(): Boolean {
        if (intent.hasExtra(Constants.DISH_UPDATE)) {
            updateDish = intent.getParcelableExtra(Constants.DISH_UPDATE)
            return true
        }
        return false
    }

    private fun setupFields() {
        if (isUpdate == true) {
            updateDish?.let { dishToUpdate ->
                supportActionBar?.title = getString(R.string.update_dish)
                binding.btnAddDish.text = getString(R.string.update_dish)
                binding.dish = dishToUpdate
                binding.ivAddImageButton.setImageResource(R.drawable.ic_edit)
            }
        }
    }

    private fun getDataFromFields(): FavDish {
        var id = 0
        var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
        var isFavourite = false

        if (isUpdate == true) {
            updateDish?.let { dish ->
                id = dish.id
                imageSource = dish.imageSource
                isFavourite = dish.isFavouriteDish
            }
        }

        val title = binding.edTitle.text.toString().trim { it <= ' ' }
        val type = binding.edType.text.toString().trim { it <= ' ' }
        val category = binding.edCategory.text.toString().trim { it <= ' ' }
        val ingredients = binding.edIngredients.text.toString().trim { it <= ' ' }
        val cookingTimeInMinutes = binding.edCookingTime.text.toString().trim { it <= ' ' }
        val cookingDirection = binding.edDirectionToCook.text.toString().trim { it <= ' ' }
        return FavDish(
            imagePath,
            imageSource,
            title,
            type,
            category,
            ingredients,
            cookingTimeInMinutes,
            cookingDirection,
            isFavourite,
            id
        )
    }

    private fun insertOrUpdateDish() {
        updateDish?.let { dish ->
            imagePath = dish.image
            Log.i(TAG, "insertOrUpdateDish: ${dish.title} ${dish.image} + L")
        }

        if (areFieldsNotEmpty()) {
            if (isUpdate == true) {
                update(getDataFromFields())
            } else if (isUpdate == false) {
                insert(getDataFromFields())
            }
            showToast("The dish has been added")
            finish()
        }
    }

    private fun insert(dishDetails: FavDish) {
        viewModel.insert(dishDetails)
    }

    private fun update(dishDetails: FavDish) {
        viewModel.update(dishDetails)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun customImageSelectionDialog() {
        customListDialog = Dialog(this)
        val bindingDialog = DialogCustomImageSelectionBinding.inflate(layoutInflater)
        customListDialog.setContentView(bindingDialog.root)
        setDialogClickListeners(bindingDialog)
        customListDialog.show()
    }

    private fun setDialogClickListeners(binding: DialogCustomImageSelectionBinding) {
        binding.tvCamera.setOnClickListener {
            buildCameraDexter()
            customListDialog.dismiss()
        }

        binding.tvGallery.setOnClickListener {
            buildGalleryDexter()
            customListDialog.dismiss()
        }
    }

    private fun buildCameraDexter() {
        Dexter.withContext(this@AddUpdateDishActivity).withPermissions(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                report?.let {
                    if (report.areAllPermissionsGranted()) {
                        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intentCamera, REQUEST_CODE_CAMERA)
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                permissions: MutableList<PermissionRequest>?,
                token: PermissionToken?
            ) = showRationalDialogPermissions()
        }).onSameThread().check()
    }

    private fun buildGalleryDexter() {
        Dexter.withContext(this@AddUpdateDishActivity).withPermission(
            Manifest.permission.READ_EXTERNAL_STORAGE,
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                val intentGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentGallery, REQUEST_CODE_GALLERY)
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                showToast(getString(R.string.permission_denied))
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) = showRationalDialogPermissions()
        }).onSameThread().check()
    }

    private fun showRationalDialogPermissions() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.dialog_error_p_off))
            .setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                try {
                    goToApplicationSettings()
                } catch (e: ActivityNotFoundException) {
                    showToast(e.printStackTrace().toString())
                }
            }.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun goToApplicationSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAMERA) {
                data?.let { dataLet ->
                    dataLet.extras?.let {
                        val thumbnail: Bitmap = it.get("data") as Bitmap
                        glideImageFromCamera(binding.ivAddImage, thumbnail)
                        imagePath = saveToInternalStorage(thumbnail)
                        Log.i(TAG, "onActivityResult: $imagePath")
                        binding.ivAddImageButton.setImageResource(R.drawable.ic_edit)
                    }
                }
            } else if (requestCode == REQUEST_CODE_GALLERY) {
                data?.let {
                    it.data?.let { dataUri ->
                        glideImageFromGallery(binding.ivAddImage, dataUri)
                        binding.ivAddImageButton.setImageResource(R.drawable.ic_edit)
                    }
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.i("LogFile", "User cancelled image selection")
        }
    }

    private fun glideImageFromCamera(imageView: ImageView, bitmap: Bitmap) {
        Glide.with(this)
            .load(bitmap)
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_connection_error)
            .centerCrop()
            .into(imageView)
    }

    private fun glideImageFromGallery(imageView: ImageView, uri: Any) {
        Glide.with(this)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    e?.let {
                        Log.e(TAG, "onLoadFailed: ${it.printStackTrace()}")
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    resource?.let {
                        imagePath = saveToInternalStorage(it.toBitmap())
                        Log.i(TAG, "onResourceReady: $imagePath")
                    }
                    return false
                }

            })
            .placeholder(R.drawable.loading_animation)
            .error(R.drawable.ic_connection_error)
            .centerCrop()
            .into(imageView)
    }

    private fun saveToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)

        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            Log.e(TAG, "saveToInternalStorage: ${e.printStackTrace()}")
        }
        return file.absolutePath
    }

    private fun showDialogListCategory(title: String, list: List<String>, selection: String) {
        val dialogListBinding = DialogListBinding.inflate(layoutInflater)

        customListDialog = Dialog(this)
        customListDialog.setContentView(dialogListBinding.root)
        dialogListBinding.tvDialogListTitle.text = title

        val adapter = DialogListItemAdapter(list, selection, OnCategoryClickListener { c, s ->
            setupTextOnCategorySelected(c, s)
            customListDialog.dismiss()
        })

        dialogListBinding.rvDialogList.layoutManager = LinearLayoutManager(this)
        dialogListBinding.rvDialogList.adapter = adapter

        customListDialog.show()
    }

    private fun setupTextOnCategorySelected(category: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> binding.edType.setText(category)
            Constants.DISH_CATEGORY -> binding.edCategory.setText(category)
            else -> binding.edCookingTime.setText(category)
        }
    }

    private fun areFieldsNotEmpty(): Boolean {
        var amountOfFilled = 0
        if (TextUtils.isEmpty(imagePath.trim())) {
            showToast("Please, select an image")
        } else {
            amountOfFilled = amountOfFilled.plus(1)
        }
        if (TextUtils.isEmpty(binding.edTitle.text.toString().trim())) {
            binding.tilTitle.error = "Please, fill the Title"
        } else {
            binding.tilTitle.error = ""
            amountOfFilled = amountOfFilled.plus(1)
        }
        if (TextUtils.isEmpty(binding.edType.text.toString().trim())) {
            binding.tilType.error = "Please, select the Type"
        } else {
            binding.tilType.error = ""
            amountOfFilled = amountOfFilled.plus(1)
        }
        if (TextUtils.isEmpty(binding.edCategory.text.toString().trim())) {
            binding.tilCategory.error = "Please, select the Category"
        } else {
            binding.tilCategory.error = ""
            amountOfFilled = amountOfFilled.plus(1)
        }
        if (TextUtils.isEmpty(binding.edIngredients.text.toString().trim())) {
            binding.tilIngredients.error = "Please, fill the Ingredients"
        } else {
            binding.tilIngredients.error = ""
            amountOfFilled = amountOfFilled.plus(1)
        }
        if (TextUtils.isEmpty(binding.edCookingTime.text.toString().trim())) {
            binding.tilCookingTime.error = "Please, select the Cooking Time"
        } else {
            binding.tilCookingTime.error = ""
            amountOfFilled = amountOfFilled.plus(1)
        }
        if (TextUtils.isEmpty(binding.edDirectionToCook.text.toString().trim())) {
            binding.tilDirectionToCook.error = "Please, fill the Directions to Cook"
        } else {
            binding.tilDirectionToCook.error = ""
            amountOfFilled = amountOfFilled.plus(1)
        }
        Log.i(TAG, "areFieldsEmpty: $amountOfFilled")
        return amountOfFilled == 7
    }
}