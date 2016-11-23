(ns photo-api.utils.pipe)

(defn set-if-not [out allow in]
  (if (contains? allow in) in out))
