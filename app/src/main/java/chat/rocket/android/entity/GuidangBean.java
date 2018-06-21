package chat.rocket.android.entity;

import java.util.List;

/**
 * Created by jumper_C on 2018/5/16.
 */

public class GuidangBean {

    /**
     * code :
     * data : {"totalSize":7,"varList":[{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"wgKPsppvcJjcbDC6A","firstName":"QQ截图20171211133721(1)","id":10000001861467,"lastName":"png","name":"QQ截图20171211133721(1).png","path":"WEINING/20171218112720728.png","size":"1.29KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"XLrcXHFifM66rcLpu","firstName":"1(1)","id":10000002162951,"lastName":"docx","name":"1(1).docx","path":"WEINING/20180302122802367.docx","size":"13.54KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"hHqgKZCjHQ8hbrcow","firstName":"magazine-unlock-06-2","id":10000002401378,"lastName":"3.926-_c254b0306b8d45719c0248b9977a71f7.jpg","name":"magazine-unlock-06-2.3.926-_c254b0306b8d45719c0248b9977a71f7.jpg","path":"WEINING/20180324181328960.jpg","size":"388.81KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"XT2bias3W82Sx2Bpe","firstName":"magazine-unlock-02-2","id":10000002401382,"lastName":"3.928-_75bbf0cae6cb4f04b381d62a4bddd23a.jpg","name":"magazine-unlock-02-2.3.928-_75bbf0cae6cb4f04b381d62a4bddd23a.jpg","path":"WEINING/20180324183306794.jpg","size":"132.34KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"S7fcHGnQWPdKGT9un","firstName":"在线预览啊","id":10000002452504,"lastName":"pptx","name":"在线预览啊.pptx","path":"WEINING/20180327153159122.pptx","size":"32.66KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"MEihXsdq1WxbCAD9Sp66vyi2LjJr88","firstName":"qk6VowyOWnNOXR0wBsfe","id":10000003110487,"lastName":"jpeg","name":"qk6VowyOWnNOXR0wBsfe.jpeg","path":"WEINING/20180516131927144.jpeg","size":"346.87KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"dntLqCwsZgjCbcjEA","firstName":"S80411-103040","id":10000003110624,"lastName":"jpg","name":"S80411-103040.jpg","path":"WEINING/20180516150451054.jpg","size":"232.79KB","uper":10000001740064,"uperName":"古小宁"}]}
     * message : 获取文件成功!
     * status : 1
     */

    private String code;
    private DataBean data;
    private String message;
    private int status;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class DataBean {
        /**
         * totalSize : 7
         * varList : [{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"wgKPsppvcJjcbDC6A","firstName":"QQ截图20171211133721(1)","id":10000001861467,"lastName":"png","name":"QQ截图20171211133721(1).png","path":"WEINING/20171218112720728.png","size":"1.29KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"XLrcXHFifM66rcLpu","firstName":"1(1)","id":10000002162951,"lastName":"docx","name":"1(1).docx","path":"WEINING/20180302122802367.docx","size":"13.54KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"hHqgKZCjHQ8hbrcow","firstName":"magazine-unlock-06-2","id":10000002401378,"lastName":"3.926-_c254b0306b8d45719c0248b9977a71f7.jpg","name":"magazine-unlock-06-2.3.926-_c254b0306b8d45719c0248b9977a71f7.jpg","path":"WEINING/20180324181328960.jpg","size":"388.81KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"XT2bias3W82Sx2Bpe","firstName":"magazine-unlock-02-2","id":10000002401382,"lastName":"3.928-_75bbf0cae6cb4f04b381d62a4bddd23a.jpg","name":"magazine-unlock-02-2.3.928-_75bbf0cae6cb4f04b381d62a4bddd23a.jpg","path":"WEINING/20180324183306794.jpg","size":"132.34KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"S7fcHGnQWPdKGT9un","firstName":"在线预览啊","id":10000002452504,"lastName":"pptx","name":"在线预览啊.pptx","path":"WEINING/20180327153159122.pptx","size":"32.66KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"MEihXsdq1WxbCAD9Sp66vyi2LjJr88","firstName":"qk6VowyOWnNOXR0wBsfe","id":10000003110487,"lastName":"jpeg","name":"qk6VowyOWnNOXR0wBsfe.jpeg","path":"WEINING/20180516131927144.jpeg","size":"346.87KB","uper":10000001740064,"uperName":"古小宁"},{"docTypeId":10000001861468,"docTypeName":"归档文件","downNumber":0,"fileSaveId":"dntLqCwsZgjCbcjEA","firstName":"S80411-103040","id":10000003110624,"lastName":"jpg","name":"S80411-103040.jpg","path":"WEINING/20180516150451054.jpg","size":"232.79KB","uper":10000001740064,"uperName":"古小宁"}]
         */

        private int totalSize;
        private List<VarListBean> varList;

        public int getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(int totalSize) {
            this.totalSize = totalSize;
        }

        public List<VarListBean> getVarList() {
            return varList;
        }

        public void setVarList(List<VarListBean> varList) {
            this.varList = varList;
        }

        public static class VarListBean {
            /**
             * docTypeId : 10000001861468
             * docTypeName : 归档文件
             * downNumber : 0
             * fileSaveId : wgKPsppvcJjcbDC6A
             * firstName : QQ截图20171211133721(1)
             * id : 10000001861467
             * lastName : png
             * name : QQ截图20171211133721(1).png
             * path : WEINING/20171218112720728.png
             * size : 1.29KB
             * uper : 10000001740064
             * uperName : 古小宁
             */

            private long docTypeId;
            private String docTypeName;
            private int downNumber;
            private String fileSaveId;
            private String firstName;
            private long id;
            private String lastName;
            private String name;
            private String path;
            private String size;
            private long uper;
            private String uperName;

            public long getDocTypeId() {
                return docTypeId;
            }

            public void setDocTypeId(long docTypeId) {
                this.docTypeId = docTypeId;
            }

            public String getDocTypeName() {
                return docTypeName;
            }

            public void setDocTypeName(String docTypeName) {
                this.docTypeName = docTypeName;
            }

            public int getDownNumber() {
                return downNumber;
            }

            public void setDownNumber(int downNumber) {
                this.downNumber = downNumber;
            }

            public String getFileSaveId() {
                return fileSaveId;
            }

            public void setFileSaveId(String fileSaveId) {
                this.fileSaveId = fileSaveId;
            }

            public String getFirstName() {
                return firstName;
            }

            public void setFirstName(String firstName) {
                this.firstName = firstName;
            }

            public long getId() {
                return id;
            }

            public void setId(long id) {
                this.id = id;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPath() {
                return path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public String getSize() {
                return size;
            }

            public void setSize(String size) {
                this.size = size;
            }

            public long getUper() {
                return uper;
            }

            public void setUper(long uper) {
                this.uper = uper;
            }

            public String getUperName() {
                return uperName;
            }

            public void setUperName(String uperName) {
                this.uperName = uperName;
            }
        }
    }
}
