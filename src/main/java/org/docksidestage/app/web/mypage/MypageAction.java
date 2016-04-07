/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.app.web.mypage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;

import org.dbflute.cbean.result.ListResultBean;
import org.docksidestage.app.web.base.WaterfrontBaseAction;
import org.docksidestage.dbflute.exbhv.ProductBhv;
import org.docksidestage.dbflute.exentity.Product;
import org.lastaflute.web.Execute;
import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.response.JsonResponse;
import org.lastaflute.web.response.StreamResponse;
import org.lastaflute.web.servlet.request.ResponseManager;

/**
 * @author jflute
 */
public class MypageAction extends WaterfrontBaseAction {

	@Resource
	protected ProductBhv productBhv;
	@Resource
	private ResponseManager responseManager;

	@Execute
	public HtmlResponse index() {
		ListResultBean<Product> productList = productBhv.selectList(cb -> {
			cb.query().addOrderBy_RegularPrice_Desc();
			cb.fetchFirst(3);
		});
		List<MypageProductBean> beans = productList.stream().map(member -> {
			return new MypageProductBean(member);
		}).collect(Collectors.toList());
		return asHtml(path_Mypage_MypageJsp).renderWith(data -> {
			data.register("beans", beans);
		});
	}

	@Execute
	public JsonResponse<List<MypageProductBean>> indexJson() {
		ListResultBean<Product> memberList = productBhv.selectList(cb -> {
			cb.query().addOrderBy_RegularPrice_Desc();
			cb.fetchFirst(3);
		});
		List<MypageProductBean> beans = memberList.stream().map(member -> {
			return new MypageProductBean(member);
		}).collect(Collectors.toList());
		return asJson(beans);
	}

	@Execute
	public StreamResponse outputstreamSample() {
		Map<String, String> paramMap = new HashMap<>();

		// 実際にはDBから
		paramMap.put("サンプル1.txt", "あああ");
		paramMap.put("サンプル2.txt", "いいい");

		/**
		 * asChunkedTransferメソッドを使用することで引数のoutと
		 * responseManager.getResponse().getOutputStream()が連結するイメージ
		 * チャンク送信になる想定ですが、asStream("").transferEncoding("chuncked").asOutput(out -> {...});
		 * のような形でも問題ありません。
		 */
		return asStream("テスト.zip").contentType("application/zip").asChunkedTransfer(out -> {

			// 複数のデータをzipにして出力する
				zipWriter(out, paramMap);
			});
	}

	/**
	 * 複数のデータをzipにして出力
	 * @param os 出力ストリーム
	 * @param paramMap パラメータ
	 */
	public void zipWriter(OutputStream os, Map<String, String> paramMap) {

		try (ZipOutputStream zos = new ZipOutputStream(os, Charset.forName("UTF-8"))) {
			for (Entry<String, String> param : paramMap.entrySet()) {
				try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

					executeQuery(baos, param.getValue());

					zos.putNextEntry(new ZipEntry(param.getKey()));
					baos.writeTo(zos);
				} finally {
					zos.closeEntry();
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void executeQuery(ByteArrayOutputStream baos, Object object) {
		// ...
	}
}
