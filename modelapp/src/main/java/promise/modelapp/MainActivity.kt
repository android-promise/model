/*
 *  Copyright 2017, Peter Vincent
 *  Licensed under the Apache License, Version 2.0, Android Promise.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package promise.modelapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ArrayMap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONObject
import promise.commons.model.List
import promise.commons.model.function.FilterFunction
import promise.commons.tx.PromiseResult
import promise.commons.util.DoubleConverter
import promise.model.CacheUtil
import promise.model.PreferenceDatabase
import promise.model.PreferenceStore
import promise.model.Store

val preferenceStore: PreferenceStore<ComplexModel> = object : PreferenceStore<ComplexModel>("name of file",
    object: DoubleConverter<ComplexModel, JSONObject, JSONObject> {
      override fun deserialize(e: JSONObject): ComplexModel = ComplexModel().apply {
        uId = e.getInt("uId")
        name = e.getString("name")
        isModel = e.getBoolean("isModel")
      }

      override fun serialize(t: ComplexModel): JSONObject = JSONObject().apply {
        put("uId", t.uId)
        put("name", t.name)
        put("isModel", t.isModel)
      }
    }) {
  override fun findIndexFunction(t: ComplexModel): FilterFunction<JSONObject> =
      FilterFunction<JSONObject> { t.uId == it.getInt("uId") }

}

val preferenceDatabase: PreferenceDatabase<Int, ComplexModel> = PreferenceDatabase("models",
    object : DoubleConverter<Int, String, String> {
      override fun deserialize(e: String): Int = e.toInt()
      override fun serialize(t: Int): String = t.toString()
    },
    object : DoubleConverter<ComplexModel, JSONObject, JSONObject> {
      override fun deserialize(e: JSONObject): ComplexModel = ComplexModel().apply {
        name = e.getString("name")
        isModel = e.getBoolean("isModel")
      }

      override fun serialize(t: ComplexModel): JSONObject = JSONObject().apply {
        put("name", t.name)
        put("isModel", t.isModel)
      }
    })

class MainActivity : AppCompatActivity() {

  private var id = 1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setSupportActionBar(toolbar)

    fab.setOnClickListener {
      val model = complexStore.one(ArrayMap<String, Any>().apply {
        put(ID_ARG, id)
      })
      select_textview.text = model.first.toString()
      id++
    }
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    val models = complexStore.all(ArrayMap<String, Any>().apply {
      put(NUMBER_ARG, 10)
      put(TIMES_ARG, 2)
    })
    val result = "${models.first.toString()} \n meta \n ${models.second}"
    main_textview.text = result

    preferenceStore.save("models",
        List.fromArray(ComplexModel().apply {
          uId = 3
          name = "name"
          isModel = true
        }, ComplexModel().apply {
          uId = 5
          name = "name1"
          isModel = false
        }),
        PromiseResult<Boolean, Throwable>()
        .withCallback {

        })

    preferenceStore.get("models", PromiseResult<Store.Extras<ComplexModel>, Throwable>()
        .withCallback {
          val items = it.all()
          // use items
        })

    CacheUtil.instance().writeObject("/complexModel/1",  ComplexModel().apply {
      uId = 5
      name = "name1"
      isModel = false
    })

    val complexModel = CacheUtil.instance().readObject("/complexModel/1", ComplexModel::class.java)
  }
}
