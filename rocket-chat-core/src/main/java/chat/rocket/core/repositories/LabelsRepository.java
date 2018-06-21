package chat.rocket.core.repositories;

import java.util.List;

import chat.rocket.core.models.Labels;
import io.reactivex.Flowable;

/**
 * Created by user on 2018/1/18.
 */

public interface LabelsRepository {
    List<Labels> getByType(String type,String companyId);
}
