package org.verzhbitski.imagehttpserver;

import java.io.IOException;
import java.util.List;

public interface ImageApi {

    Response getImage(String imageName) throws IOException;

    Response uploadImage(List<Integer> body) throws IOException;
}
