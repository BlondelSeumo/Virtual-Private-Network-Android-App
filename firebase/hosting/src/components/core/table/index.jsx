import * as React from 'react';
import _ from 'lodash'
import {Select, Table as Table2} from 'antd';
import queryString from "query-string";
import {withRouter} from "react-router-dom";
import {firestore} from "@app/services/firebase";
import firebase from "firebase";
import UIButton from "@app/components/core/button";

type PropsType = {
  service: any,
  columns: any[],
  sort?: Object,
  customComp?: Object,
  defineCols?: Array,
  customColsName?: Object,
  hiddenCols?: Array,
  search?: Object,
  payload?: Object,
  isReload?: string,
  className?: string,
  staticData?: any[],
  staticHeader?: any[],
  headerWidth?: Object;
  isHiddenPg?: boolean;
  isAutoFetchData?: boolean;
  isAddParams?: boolean;
  onSelectAll?: Function;
}

type StatesType = {
  columns: any[],
  rows: any[],
  pagination: Object,
  loading: boolean,
  page: number,
  limit: number,
  sort: string,
  isSort: boolean,
  payload: any,
  search: Object,
}

class UITable extends React.PureComponent<PropsType, StatesType> {
  constructor(props) {
    super(props);

    this.state = {
      columns: [],
      rows: [],
      pagination: {},
      loading: false,
      page: 1,
      limit: 10,
      sort: '',
      isSort: false,
      payload: undefined,
      search: {},
      total: 10,
      lastCursor: undefined,
      isNext: true,
      cacheRows: {}
    }
  }

  componentDidMount() {
    const {
      staticData,
      staticHeader,
      location,
      isAutoFetchData,
      isAddParams,
      payload
    } = this.props;

    if (isAutoFetchData) {
      let currentQuery = queryString.parse(location.search);
      const page = this.props.page || (currentQuery?.page || 1);
      const limit = currentQuery?.limit || 10;

      if (!staticData && !staticHeader) {
        this.setState({
          page: parseInt(page, 0),
          limit: parseInt(limit, 0),
          loading: true,
          payload
        }, () => {
          this.fetchTableData();
          if (isAddParams) this.pushToParams(page, limit);
        });
      } else {
        this.setState({
          rows: staticData,
          columns: staticHeader,
          page: parseInt(page, 0),
          limit: parseInt(limit, 0),
          payload,
          pagination: {
            total: staticData.length,
            per_page: 10,
          },
        }, () => isAddParams && this.pushToParams(page, limit));
      }
    }
  }

  componentDidUpdate(prevProps, prevState) {
    const {
      payload,
      isReload,
      staticData,
      columns,
      staticHeader,
      search
    } = this.props;
    const {
      rows
    } = this.state

    if (!_.isEqual(payload, prevProps.payload) && !_.isEqual(payload, prevState.payload)) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.changeStateFetchData(payload);
    }

    if (!_.isEqual(search, prevProps.search) && !_.isEqual(search, prevState.search)) {
      this.setState({
        search,
        page: 1,
        loading: true,
        lastCursor: undefined,
        isNext: true,
        rows: [],
        cacheRows: {}
      }, () => this.fetchTableData());
    }

    if (isReload && isReload !== prevProps.isReload) {
      this.changeStateFetchData(payload);
    }

    if (columns && !_.isEqual(columns, prevProps.columns)) {
      this.changeStateFetchData(payload);
    }

    if (staticData !== prevProps.staticData) {
      // eslint-disable-next-line react/no-did-update-set-state
      this.setState({
        rows: staticData,
        columns: staticHeader,
      });
    }
  }

  changeStateFetchData = (payload) => {
    this.setState({
      payload,
      page: 1,
      loading: true,
      lastCursor: undefined,
      isNext: true
    }, () => this.fetchTableData());
  };

  fetchTableData = () => {
    const {
      limit,
      page,
      sort,
      payload,
      search,
      lastCursor,
      isNext,
      cacheRows,
      rows: currentRows,
    } = this.state;

    const {
      service,
      columns,
    } = this.props;

    if (typeof service === "string" && isNext) {
      let ref = firestore.collection(service)
      let refCount = firestore.collection(service)

      if (lastCursor && isNext) {
        ref = ref.orderBy(firebase.firestore.FieldPath.documentId()).startAfter(lastCursor?.id).limit(limit)
      }

      if (limit && !lastCursor) {
        ref = ref.limit(limit).orderBy(firebase.firestore.FieldPath.documentId())
      }

      if (payload) {
        const objPayload = Object.entries(payload)

        for (const filter of objPayload) {
          if (filter?.[1] !== "" && filter?.[1] !== null && filter?.[1] !== undefined) {
            ref = ref.where(filter?.[0], "==", filter?.[1])
            refCount = refCount.where(filter?.[0], "==", filter?.[1])
          }
        }
      }

      refCount.get()
        .then((snap) => {
          this.setState({
            total: snap.docs.length
          }, () => {
            ref.get()
              .then((result) => {
                let tmpCacheRows = {...cacheRows}
                if (result) {

                  let rows = []
                  result.forEach((doc) => {
                    rows.push({...doc.data(), id: doc?.id})
                    tmpCacheRows = {
                      ...tmpCacheRows,
                      [doc?.id]: doc
                    }
                  })

                  if (search?.key && search?.value) {
                    rows = rows.filter((row) => row?.[search?.key]?.toLowerCase().indexOf(search?.value?.toLowerCase()) >= 0)
                  }
                  this.setState({
                    rows,
                    columns,
                    loading: false,
                    lastCursor: result?.docs?.[result?.docs.length - 1],
                    cacheRows: tmpCacheRows
                  }, () => {
                    console.log(this.state)
                  });
                }
              }).catch(() => {
              this.setState({
                loading: false,
              });
            });
          })
        })
    } else {
      const from = (page -1) * limit;
      const to = limit*page;
      const tmpFirestoreRows = Object.values(cacheRows || [])?.slice(from, to) || []
      const tmpLastCursor = tmpFirestoreRows?.[tmpFirestoreRows.length - 1]
      const tmpRows = tmpFirestoreRows.reduce((arr, doc) => ([...arr, {id: doc?.id, ...doc?.data()}]), [])

      setTimeout(() => {
        this.setState({
          rows: tmpRows,
          columns,
          loading: false,
          lastCursor: tmpLastCursor
        });
      }, 1000)
    }
  };

  makeColumns = (columns) => {
    const {
      hiddenCols,
    } = this.props;

    let definedColumns = [];

    if (columns && columns.length > 0) {
      const newColumns = this.hiddenColumn(columns);

      for (let i = 0; i < newColumns.length; i++) {
        if (!hiddenCols[newColumns[i].code]) {
          definedColumns = [
            ...definedColumns,
            {
              title: this.makeColumnName(newColumns[i]),
              dataIndex: newColumns[i].code,
              ...this.handlecustomComp(newColumns[i].code),
              width: 300,
              key: newColumns[i].code,
            },
          ];
        }
      }
    }

    return definedColumns;
  };

  makeColumnName = (column) => {
    const {
      customColsName,
    } = this.props;

    if (!customColsName[column.code]) return column.name;
    if (customColsName[column.code]) return customColsName[column.code];

    return '';
  };

  hiddenColumn = (oldColumns) => {
    const {
      hiddenCols,
    } = this.props;

    let newColumns = [];

    for (let i = 0; i < oldColumns.length; i++) {
      if (hiddenCols.filter((cl) => cl === oldColumns[i].code).length === 0) {
        newColumns = [
          ...newColumns,
          oldColumns[i],
        ];
      }
    }

    return newColumns;
  };

  makecustomCols = (headers) => {
    const {
      defineCols,
      headerWidth
    } = this.props;
    const {
      rows
    } = this.state;

    let definedcustomCols = [];

    for (let i = 0; i < defineCols.length; i++) {
      definedcustomCols = [
        ...definedcustomCols,
        {
          title: !this.isFunction(defineCols[i].name) ? defineCols[i].name : defineCols[i].name(rows),
          dataIndex: defineCols[i].code,
          key: defineCols[i].code,
          ...this.handlecustomComp(defineCols[i].code),
          width: headerWidth[defineCols[i]?.code || ''] || 300,
        },
      ];
    }

    return definedcustomCols;
  };

  definePositionOfColumn = (sort, headers, i) => {
    const {
      defineCols
    } = this.props;

    if (!sort) return defineCols.length + i + 1;

    if (sort === 'end') {
      return headers.length + defineCols.length + 1
    }

    return sort;
  };

  handleColumnSort = (columnName) => {
    const {
      sort,
    } = this.props;

    if (sort[columnName]) {
      return {
        sorter: true,
      };
    }

    return {};
  };

  handlecustomComp = (columnName) => {
    const {
      customComp,
    } = this.props;

    if (customComp[columnName]) {
      return {
        render: (text, row, index) => {
          const dom = customComp[columnName]({text, row, index})

          return dom
        },
      };
    }

    return {};
  };

  pushToParams = (page, limit) => {
    const {
      location,
      history,
    } = this.props;

    const currentQuery = queryString.parse(location.search);

    currentQuery.page = page;
    currentQuery.limit = limit;

    history.push(`${location.pathname}?${queryString.stringify(currentQuery)}`);
  };

  onChangePage = (page) => {
    const {
      staticData,
      isAddParams
    } = this.props;
    const {
      page: oldPage
    } = this.state;

    this.setState({
      page,
      isNext: page > oldPage,
      loading: !staticData,
    }, () => {
      if (!staticData) {
        this.fetchTableData();
      }

      if (isAddParams) this.pushToParams(page, this.state.limit);
    });
  };

  onChangePageSize = (limit) => {
    const {
      staticData,
      isAddParams,
    } = this.props;

    this.setState({
      limit,
      page: 1,
      lastCursor: undefined,
      isNext: true,
      loading: !staticData,
      cacheRows: {},
    }, () => {
      if (!staticData) {
        this.fetchTableData();
      }

      if (isAddParams) this.pushToParams(1, limit);
    });
  };

  antIcon = () => <Loading/>;

  onChange = (pg, filters, sorter) => {
    if (sorter && sorter.order && sorter.columnKey) {
      this.setNewSortState(sorter.columnKey, this.sortType(sorter.order), true);
    } else {
      this.setNewSortState('', '', false);
    }
  };

  convertArrayColumnsToAntdColumns = () => {
    const {
      columns
    } = this.state;

    return _.orderBy([...this.makeColumns(columns), ...this.makecustomCols(columns)], ['sort'], ['asc'])
  };

  isFunction = (functionToCheck) => {
    return functionToCheck && {}.toString.call(functionToCheck) === '[object Function]';
  };

  configSelectAll = () => {
    const {
      onSelectAll
    } = this.props;
    return {
      onChange: (selectedRowKeys, selectedRows) => {
        onSelectAll(selectedRowKeys, selectedRows);
      },
    };
  }

  render() {
    const {
      pagination,
      loading,
      limit,
      page,
      rows,
      total
    } = this.state;

    const {
      className,
      isHiddenPg,
      onSelectAll
    } = this.props;

    const header = this.convertArrayColumnsToAntdColumns();

    return (
      <div className="w-full">
        <Table2
          {...(onSelectAll ? {
            rowSelection: {
              type: "checkbox",
              ...this.configSelectAll(),
            }
          } : {})}
          rowKey="id"
          rowClassName="shdvn-table-row"
          loading={loading}
          pagination={false}
          className={`shdvn-table ${className}`}
          dataSource={rows}
          columns={header}
        />
        <div className="flex justify-between items-center mt-3">
          <Select
            defaultValue={limit}
            onChange={(e) => this.onChangePageSize(e)}
            style={{width: 'fit-content', minWidth: 120}}>
            <Select.Option value={10}>10 / page</Select.Option>
            <Select.Option value={20}>20 / page</Select.Option>
            <Select.Option value={50}>50 / page</Select.Option>
            <Select.Option value={100}>100 / page</Select.Option>
          </Select>
          <div className="flex items-center">
            {
              page !== 1 && (
                <UIButton
                  onClick={() => this.onChangePage(page - 1 < 0 ? 1: page - 1)}
                  className="mr-3 gray">
                  Previous
                </UIButton>
              )
            }
            {
              page !== Math.ceil(total / limit) && (
                <UIButton
                  onClick={() => this.onChangePage(page + 1 > Math.ceil(total / limit) ? Math.ceil(total / limit) : page + 1)}
                  className="standard">
                  Next
                </UIButton>
              )
            }
          </div>
        </div>
      </div>
    );
  }
}

UITable.defaultProps = {
  sort: {},
  customComp: {},
  hiddenCols: [],
  search: {},
  defineCols: [],
  customColsName: {},
  payload: undefined,
  isReload: '',
  staticData: null,
  staticHeader: null,
  className: '',
  headerWidth: {},
  isScroll: false,
  isHiddenPg: false,
  isAutoFetchData: true,
  isAddParams: true,
  includes: [],
  onSelectAll: undefined // callback,
};

export default withRouter(UITable);