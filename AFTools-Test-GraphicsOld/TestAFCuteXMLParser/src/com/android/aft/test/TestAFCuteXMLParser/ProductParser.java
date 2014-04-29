/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package com.android.aft.test.TestAFCuteXMLParser;

import java.util.ArrayList;

import com.android.aft.AFCuteXmlParser.AFCuteXmlParser;
import com.android.aft.AFCuteXmlParser.AFNodeAction;
import com.android.aft.AFCuteXmlParser.AFNodeActionAtDepth;
import com.android.aft.AFCuteXmlParser.AFNodeActionSonOf;
import com.android.aft.AFCuteXmlParser.AFNodeActionSonOfAtDepth;

public class ProductParser extends AFCuteXmlParser
{

	public ProductParser()
	{
		super();

        addNodeAction(new AFNodeAction<Object>("Category") {
            @Override
            public void onNode(Object cookie) {
                ArrayList<Category> categories = new ArrayList<Category>();
                read_children(categories);
                getContext().getResult().setData(categories);
            }
        });

		// Action on node 'return'
		addNodeAction(new AFNodeAction<ArrayList<Category>>("return") {
			@Override
            public void onNode(ArrayList<Category> categories) {
				Category category = new Category();
				read_children(category);
				categories.add(category);
			}
		});

		addNodeAction(new AFNodeActionSonOfAtDepth<Category>("initialRef", 7, "parentRef") {
//			public void onNode(Object cookie){
//				((Category)cookie).initialRef(read_content());
//			}
            @Override
            public void onNode(Category category) {
                category.setInitialRef(read_content());
            }
		});

		addNodeAction(new AFNodeActionAtDepth<Category>("categoryIdBis", 7){
			@Override
            public void onNode(Category category) {
			    category.setCategoryIdBis(read_content());
			}
		});

		addNodeAction(new AFNodeActionSonOf<Category>("element", "parentTwo"){
			@Override
            public void onNode(Category category) {
			    category.setElement(read_content());
			}
		});

		// Action on node 'categoryId'
		addNodeAction(new AFNodeActionSonOf<Category>("categoryId", "return") {
			@Override
            public void onNode(Category category) {
				category.setId(read_content());
			};
		});

//		// Action on node 'categoryId' son on 'categoryName'
//        addNodeAction(new NFNodeActionUnderNodeAtRelativeDepth<Category>("categoryId", "return", 2) {
//            public void onNode(Category category) {
//                category.name(read_content());
//            };
//        });

		// Action on node 'categoryName'
		addNodeAction(new AFNodeAction<Category>("categoryName") {
			@Override
            public void onNode(Category category) {
			    category.setName(read_content());
			};
		});

		// Action on node 'description'
		addNodeAction(new AFNodeAction<Category>("description") {
			@Override
            public void onNode(Category category) {
			    category.setDescription(read_content());
			};
		});

		// Action on node 'hasChild'
		addNodeAction(new AFNodeAction<Category>("hasChild") {
			@Override
            public void onNode(Category category) {
			    category.setHasChild(read_content().equals("1"));
			};
		});

	}

}
